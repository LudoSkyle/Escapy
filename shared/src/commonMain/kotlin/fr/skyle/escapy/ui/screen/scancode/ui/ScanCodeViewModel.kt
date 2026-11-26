package fr.skyle.escapy.ui.screen.scancode.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.usecase.game.JoinGameUseCase
import fr.skyle.escapy.domain.validation.JoinCodeQr
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.core.error.toGameError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * The QR code of a game read by the camera, then the game joined. The camera reads the same code many
 * times a second: reading pauses before anything is done with it, and a content refused is let be,
 * for the server not to count it again and again: an unknown code counts towards the attempts it
 * allows a user. A refusal for good (no such game, a full one, not a game at all) holds while the
 * screen is open; a failure that may not happen again (the network) holds only a while.
 */
class ScanCodeViewModel(
    private val joinGameUseCase: JoinGameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanCodeUIState())
    val uiState: StateFlow<ScanCodeUIState> = _uiState.asStateFlow()

    private val _event = Channel<ScanCodeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    // Contents refused for good, ignored until the screen closes
    private val refusedContents = mutableSetOf<String>()

    // The content that failed last for a reason that may pass, ignored for a while
    private var failedContent: String? = null
    private var failureExpiry: Job? = null

    fun onCameraUnavailable() {
        _uiState.update { uiState ->
            uiState.copy(isCameraUnavailable = true)
        }
    }

    fun onQrCodeRead(content: String) {
        if (!_uiState.value.isReading || content in refusedContents || content == failedContent) return

        val joinCode = JoinCodeQr.joinCodeOf(content = content)
        if (joinCode == null) {
            refuse(content = content, error = GameError.NOT_A_GAME_QR_CODE)
            return
        }

        // Before anything else, the next readings of the same code come at once
        _uiState.update { uiState ->
            uiState.copy(isReading = false)
        }
        viewModelScope.launch {
            try {
                val gameId = joinGameUseCase(code = joinCode)

                _event.send(ScanCodeEvent.OpenGame(gameId = gameId))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to join a scanned game" }

                refuse(content = content, error = exception.toGameError())
                _uiState.update { uiState ->
                    uiState.copy(isReading = true)
                }
            }
        }
    }

    private fun refuse(content: String, error: GameError) {
        if (error.mayPass) {
            failedContent = content
            failureExpiry?.cancel()
            failureExpiry = viewModelScope.launch {
                delay(FAILURE_DURATION)
                failedContent = null
            }
        } else {
            refusedContents += content
        }
        viewModelScope.launch {
            _event.send(ScanCodeEvent.ShowError(error = error))
        }
    }

    data class ScanCodeUIState(
        // False while a scanned game is being joined
        val isReading: Boolean = true,
        val isCameraUnavailable: Boolean = false,
    ) {
        val isJoining: Boolean
            get() = !isReading
    }

    sealed interface ScanCodeEvent {
        data class OpenGame(val gameId: String) : ScanCodeEvent
        data class ShowError(val error: GameError) : ScanCodeEvent
    }

    private companion object {
        val FAILURE_DURATION = 5.seconds

        val GameError.mayPass: Boolean
            get() = this == GameError.NETWORK
    }
}
