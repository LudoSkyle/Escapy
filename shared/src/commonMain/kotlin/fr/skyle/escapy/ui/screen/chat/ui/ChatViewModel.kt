package fr.skyle.escapy.ui.screen.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.exception.MessageRefusedException
import fr.skyle.escapy.domain.model.ChatMessage
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.usecase.chat.SendMessageUseCase
import fr.skyle.escapy.domain.usecase.chat.WatchMessagesUseCase
import fr.skyle.escapy.domain.usecase.game.ExpireGameUseCase
import fr.skyle.escapy.domain.usecase.game.WatchGameUseCase
import fr.skyle.escapy.domain.validation.ChatRules
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.core.error.toGameError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The chat of a game, live: the latest messages, and a message sent at once. The message shows in
 * the chat before the server acknowledges it: the field empties without waiting, and a message the
 * server refuses comes back to the field, with the reason. A refusal asks the server to record a time
 * up, the usual reason, so that the field goes.
 */
class ChatViewModel(
    private val gameId: String,
    private val watchGameUseCase: WatchGameUseCase,
    private val watchMessagesUseCase: WatchMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val expireGameUseCase: ExpireGameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUIState())
    val uiState: StateFlow<ChatUIState> = _uiState.asStateFlow()

    private val _event = Channel<ChatEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            watchGameUseCase(gameId = gameId)
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the game" }
                }
                .collect { state ->
                    _uiState.update { uiState ->
                        uiState.copy(state = state)
                    }
                }
        }
        viewModelScope.launch {
            watchMessagesUseCase(gameId = gameId)
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the chat" }
                }
                .collect { messages ->
                    _uiState.update { uiState ->
                        uiState.copy(messages = messages)
                    }
                }
        }
    }

    /** Sends [text], unless it is not a message; the screen empties its field at once. */
    fun onSendClicked(text: String) {
        if (!ChatRules.isValidMessage(text)) return
        viewModelScope.launch {
            try {
                sendMessageUseCase(gameId = gameId, text = text)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to send a message" }

                _event.send(ChatEvent.SendFailed(text = text, error = exception.toGameError()))
                if (exception is MessageRefusedException) recordTimeUp()
            }
        }
    }

    // A refusal most often means the time is up, which only the game screen records otherwise: the
    // server checks it by its own clock, and the field goes once the game reads finished
    private suspend fun recordTimeUp() {
        try {
            expireGameUseCase(gameId = gameId)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Logger.w(throwable = exception) { "Failed to record the time up" }
        }
    }

    data class ChatUIState(
        // Null until the first read
        val state: GameState? = null,
        val messages: List<ChatMessage>? = null,
    ) {
        /** Whether the user may write: while the game is played, the rules check the rest. */
        val isOpen: Boolean
            get() = (state as? GameState.Available)?.game?.status in setOf(GameStatus.RUNNING, GameStatus.PAUSED)
    }

    sealed interface ChatEvent {
        /** A message did not go: its text comes back to the field, if the user typed nothing since. */
        data class SendFailed(val text: String, val error: GameError) : ChatEvent
    }
}
