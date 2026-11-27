package fr.skyle.escapy.ui.screen.waitingroom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.Mission
import fr.skyle.escapy.domain.usecase.game.CancelGameUseCase
import fr.skyle.escapy.domain.usecase.game.KickPlayerUseCase
import fr.skyle.escapy.domain.usecase.game.LeaveGameUseCase
import fr.skyle.escapy.domain.usecase.game.StartGameUseCase
import fr.skyle.escapy.domain.usecase.game.WatchGameUseCase
import fr.skyle.escapy.domain.usecase.mission.WatchMissionsUseCase
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.core.error.toGameError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A game before it starts, live: its code, its game master and its players. The game master starts
 * the game, excludes a player or cancels the game; a player leaves it. The screen asks for a
 * confirmation first, in its dialogs: the actions here run once confirmed, starting apart. Once the
 * game starts, for the game master or for a player, the screen gives way to the game.
 */
class WaitingRoomViewModel(
    private val gameId: String,
    private val watchGameUseCase: WatchGameUseCase,
    private val leaveGameUseCase: LeaveGameUseCase,
    private val kickPlayerUseCase: KickPlayerUseCase,
    private val cancelGameUseCase: CancelGameUseCase,
    private val startGameUseCase: StartGameUseCase,
    private val watchMissionsUseCase: WatchMissionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaitingRoomUIState())
    val uiState: StateFlow<WaitingRoomUIState> = _uiState.asStateFlow()

    private val _event = Channel<WaitingRoomEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private var watching: Job? = null
    private var watchingMissions: Job? = null

    // The game is told started once: each later change of the game would tell it again
    private var isStartTold = false

    init {
        watch()
    }

    private fun watch() {
        watching = viewModelScope.launch {
            watchGameUseCase(gameId = gameId)
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the game" }
                }
                .collect { state ->
                    _uiState.update { uiState ->
                        uiState.copy(state = state)
                    }
                    // The missions the game master prepares: theirs only, a player's observation would be refused
                    if (state is GameState.Available && state.game.isMaster && watchingMissions == null) {
                        watchMissions()
                    }
                    if (state is GameState.Available && state.game.status != GameStatus.WAITING && !isStartTold) {
                        isStartTold = true
                        _event.send(WaitingRoomEvent.Started)
                    }
                }
        }
    }

    private fun watchMissions() {
        watchingMissions = viewModelScope.launch {
            watchMissionsUseCase(gameId = gameId, isMaster = true)
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the missions" }
                }
                .collect { missions ->
                    _uiState.update { uiState ->
                        uiState.copy(missions = missions)
                    }
                }
        }
    }

    fun onStartClicked() {
        if (_uiState.value.isStarting) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isStarting = true)
            }

            // Started, the game tells it by its status, which the observation above follows
            try {
                startGameUseCase(gameId = gameId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to start the game" }

                _event.send(WaitingRoomEvent.ShowError(error = exception.toGameError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isStarting = false)
                }
            }
        }
    }

    fun onKickConfirmed(playerId: String) {
        if (_uiState.value.isKicking) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isKicking = true)
            }

            try {
                kickPlayerUseCase(gameId = gameId, playerId = playerId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to exclude a player" }

                _event.send(WaitingRoomEvent.ShowError(error = exception.toGameError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isKicking = false)
                }
                _event.send(WaitingRoomEvent.ActionFinished)
            }
        }
    }

    fun onLeaveConfirmed() {
        if (_uiState.value.isLeaving) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isLeaving = true)
            }
            close(failureMessage = "Failed to leave the game") {
                leaveGameUseCase(gameId = gameId)
            }
            _uiState.update { uiState ->
                uiState.copy(isLeaving = false)
            }
        }
    }

    fun onCancelConfirmed() {
        if (_uiState.value.isCancelling) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isCancelling = true)
            }
            close(failureMessage = "Failed to cancel the game") {
                cancelGameUseCase(gameId = gameId)
            }
            _uiState.update { uiState ->
                uiState.copy(isCancelling = false)
            }
        }
    }

    /**
     * Runs what takes the user out of the game, then closes the screen; tells a failure instead. The
     * game stops being observed first: the user going is no news to tell them, and an observation
     * refused meanwhile would leave the Android SDK late on the others (see docs/KMP/01_MODULE_DATA.md).
     */
    private suspend fun close(failureMessage: String, action: suspend () -> Unit) {
        watching?.cancelAndJoin()
        watchingMissions?.cancelAndJoin()
        watchingMissions = null
        try {
            action()

            _event.send(WaitingRoomEvent.Closed)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Logger.w(throwable = exception) { failureMessage }

            watch()
            _event.send(WaitingRoomEvent.ShowError(error = exception.toGameError()))
            _event.send(WaitingRoomEvent.ActionFinished)
        }
    }

    data class WaitingRoomUIState(
        // Null until the first read
        val state: GameState? = null,
        // The missions the game master prepares; null for a player, or until read
        val missions: List<Mission>? = null,
        val isStarting: Boolean = false,
        val isKicking: Boolean = false,
        val isLeaving: Boolean = false,
        val isCancelling: Boolean = false,
    )

    sealed interface WaitingRoomEvent {
        data object Closed : WaitingRoomEvent

        /** The game started: the screen gives way to the game. */
        data object Started : WaitingRoomEvent

        /** An action confirmed in a dialog is over, done or failed: the dialog closes. */
        data object ActionFinished : WaitingRoomEvent

        data class ShowError(val error: GameError) : WaitingRoomEvent
    }
}
