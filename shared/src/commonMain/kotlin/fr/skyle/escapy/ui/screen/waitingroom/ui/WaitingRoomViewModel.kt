package fr.skyle.escapy.ui.screen.waitingroom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.usecase.game.CancelGameUseCase
import fr.skyle.escapy.domain.usecase.game.KickPlayerUseCase
import fr.skyle.escapy.domain.usecase.game.LeaveGameUseCase
import fr.skyle.escapy.domain.usecase.game.WatchGameUseCase
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
 * A game before it starts, live: its code, its game master and its players. The game master
 * excludes a player or cancels the game; a player leaves it.
 */
class WaitingRoomViewModel(
    private val gameId: String,
    private val watchGameUseCase: WatchGameUseCase,
    private val leaveGameUseCase: LeaveGameUseCase,
    private val kickPlayerUseCase: KickPlayerUseCase,
    private val cancelGameUseCase: CancelGameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaitingRoomUIState())
    val uiState: StateFlow<WaitingRoomUIState> = _uiState.asStateFlow()

    private val _event = Channel<WaitingRoomEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private var watching: Job? = null

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
                        uiState.copy(
                            state = state,
                            // A player who left meanwhile is no longer to exclude
                            playerToKick = uiState.playerToKick?.takeIf { player ->
                                state is GameState.Available && player in state.players
                            },
                        )
                    }
                }
        }
    }

    fun onKickClicked(player: GameMember) {
        _uiState.update { uiState ->
            uiState.copy(playerToKick = player)
        }
    }

    fun onKickDismissed() {
        if (_uiState.value.isKicking) return
        _uiState.update { uiState ->
            uiState.copy(playerToKick = null)
        }
    }

    fun onKickConfirmed() {
        val player = _uiState.value.playerToKick ?: return
        if (_uiState.value.isKicking) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isKicking = true)
            }

            try {
                kickPlayerUseCase(gameId = gameId, playerId = player.userId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to exclude a player" }

                _event.send(WaitingRoomEvent.ShowError(error = exception.toGameError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isKicking = false, playerToKick = null)
                }
            }
        }
    }

    fun onLeaveClicked() {
        _uiState.update { uiState ->
            uiState.copy(isLeaveDialogShown = true)
        }
    }

    fun onLeaveDismissed() {
        if (_uiState.value.isLeaving) return
        _uiState.update { uiState ->
            uiState.copy(isLeaveDialogShown = false)
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
                uiState.copy(isLeaving = false, isLeaveDialogShown = false)
            }
        }
    }

    fun onCancelClicked() {
        _uiState.update { uiState ->
            uiState.copy(isCancelDialogShown = true)
        }
    }

    fun onCancelDismissed() {
        if (_uiState.value.isCancelling) return
        _uiState.update { uiState ->
            uiState.copy(isCancelDialogShown = false)
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
                uiState.copy(isCancelling = false, isCancelDialogShown = false)
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
        try {
            action()

            _event.send(WaitingRoomEvent.Closed)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Logger.w(throwable = exception) { failureMessage }

            watch()
            _event.send(WaitingRoomEvent.ShowError(error = exception.toGameError()))
        }
    }

    data class WaitingRoomUIState(
        // Null until the first read
        val state: GameState? = null,
        // The player the game master is asked to confirm the exclusion of
        val playerToKick: GameMember? = null,
        val isKicking: Boolean = false,
        val isLeaveDialogShown: Boolean = false,
        val isLeaving: Boolean = false,
        val isCancelDialogShown: Boolean = false,
        val isCancelling: Boolean = false,
    )

    sealed interface WaitingRoomEvent {
        data object Closed : WaitingRoomEvent
        data class ShowError(val error: GameError) : WaitingRoomEvent
    }
}
