package fr.skyle.escapy.ui.screen.creategame.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.usecase.game.CreateGameUseCase
import fr.skyle.escapy.domain.validation.GameRules
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.core.error.toGameError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** A new game: a title, a duration and a maximum of players, within the bounds of the server. */
class CreateGameViewModel(
    private val createGameUseCase: CreateGameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGameUIState())
    val uiState: StateFlow<CreateGameUIState> = _uiState.asStateFlow()

    private val _event = Channel<CreateGameEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun onTitleEdited() {
        _uiState.update { uiState ->
            uiState.copy(titleError = null)
        }
    }

    fun onDurationDecreased() {
        _uiState.update { uiState ->
            uiState.copy(
                durationMinutes = (uiState.durationMinutes - GameRules.DURATION_STEP_MINUTES)
                    .coerceAtLeast(GameRules.DURATION_MIN_MINUTES),
            )
        }
    }

    fun onDurationIncreased() {
        _uiState.update { uiState ->
            uiState.copy(
                durationMinutes = (uiState.durationMinutes + GameRules.DURATION_STEP_MINUTES)
                    .coerceAtMost(GameRules.DURATION_MAX_MINUTES),
            )
        }
    }

    // Below the lowest maximum, there is none
    fun onMaxPlayersDecreased() {
        _uiState.update { uiState ->
            val maxPlayers = uiState.maxPlayers ?: return@update uiState
            uiState.copy(maxPlayers = if (maxPlayers > GameRules.MAX_PLAYERS_MIN) maxPlayers - 1 else null)
        }
    }

    fun onMaxPlayersIncreased() {
        _uiState.update { uiState ->
            val maxPlayers = uiState.maxPlayers
            uiState.copy(
                maxPlayers = if (maxPlayers == null) {
                    GameRules.MAX_PLAYERS_MIN
                } else {
                    (maxPlayers + 1).coerceAtMost(GameRules.PLAYER_LIMIT)
                },
            )
        }
    }

    // The title being written stays in the screen, where each key is shown at once: given on creating
    fun onCreateClicked(title: String) {
        if (title.isBlank() || _uiState.value.isCreating) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isCreating = true)
            }

            try {
                val gameId = createGameUseCase(
                    title = title,
                    durationMinutes = _uiState.value.durationMinutes,
                    maxPlayers = _uiState.value.maxPlayers,
                )

                _event.send(CreateGameEvent.Created(gameId = gameId))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to create a game" }

                val error = exception.toGameError()
                if (error == GameError.INVALID_GAME_TITLE) {
                    _uiState.update { uiState ->
                        uiState.copy(titleError = error)
                    }
                } else {
                    _event.send(CreateGameEvent.ShowError(error = error))
                }
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isCreating = false)
                }
            }
        }
    }

    data class CreateGameUIState(
        val durationMinutes: Int = GameRules.DURATION_DEFAULT_MINUTES,
        // Null for no maximum: the server lets in up to GameRules.PLAYER_LIMIT players
        val maxPlayers: Int? = null,
        val titleError: GameError? = null,
        val isCreating: Boolean = false,
    ) {
        val canShorten: Boolean
            get() = durationMinutes > GameRules.DURATION_MIN_MINUTES

        val canLengthen: Boolean
            get() = durationMinutes < GameRules.DURATION_MAX_MINUTES

        val canLowerMaxPlayers: Boolean
            get() = maxPlayers != null

        val canRaiseMaxPlayers: Boolean
            get() = maxPlayers == null || maxPlayers < GameRules.PLAYER_LIMIT
    }

    sealed interface CreateGameEvent {
        data class Created(val gameId: String) : CreateGameEvent
        data class ShowError(val error: GameError) : CreateGameEvent
    }
}
