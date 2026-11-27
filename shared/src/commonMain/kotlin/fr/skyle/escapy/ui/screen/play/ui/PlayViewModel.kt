package fr.skyle.escapy.ui.screen.play.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.ChatMessage
import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.usecase.chat.WatchMessagesUseCase
import fr.skyle.escapy.domain.usecase.clock.EstimateServerClockOffsetUseCase
import fr.skyle.escapy.domain.usecase.game.AddTimeUseCase
import fr.skyle.escapy.domain.usecase.game.CancelGameUseCase
import fr.skyle.escapy.domain.usecase.game.EndGameUseCase
import fr.skyle.escapy.domain.usecase.game.ExpireGameUseCase
import fr.skyle.escapy.domain.usecase.game.KickPlayerUseCase
import fr.skyle.escapy.domain.usecase.game.LeaveGameUseCase
import fr.skyle.escapy.domain.usecase.game.PauseGameUseCase
import fr.skyle.escapy.domain.usecase.game.ResumeGameUseCase
import fr.skyle.escapy.domain.usecase.game.WatchGameUseCase
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.core.error.toGameError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * A started game, live, then its recap. The timer shows the time left by the clock of the server,
 * which the device estimates (see docs/Common/02_CONVENTIONS.md): it is computed again from the
 * clock at each tick, never counted down by itself. When it reaches zero, the app asks the server to
 * record it; the server decides by its own clock.
 *
 * The game master pauses, resumes, adds time, ends the game, excludes a player and deletes the
 * game once over; a player leaves it. The latest message of the chat shows, to open it. The dialogs of the screen ask first: these actions run once
 * confirmed, pausing and resuming apart.
 */
class PlayViewModel(
    private val gameId: String,
    private val watchGameUseCase: WatchGameUseCase,
    private val watchMessagesUseCase: WatchMessagesUseCase,
    private val estimateServerClockOffsetUseCase: EstimateServerClockOffsetUseCase,
    private val pauseGameUseCase: PauseGameUseCase,
    private val resumeGameUseCase: ResumeGameUseCase,
    private val addTimeUseCase: AddTimeUseCase,
    private val endGameUseCase: EndGameUseCase,
    private val expireGameUseCase: ExpireGameUseCase,
    private val kickPlayerUseCase: KickPlayerUseCase,
    private val leaveGameUseCase: LeaveGameUseCase,
    private val cancelGameUseCase: CancelGameUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayUIState())
    val uiState: StateFlow<PlayUIState> = _uiState.asStateFlow()

    private val _event = Channel<PlayEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private var watching: Job? = null
    private var watchingChat: Job? = null

    // Kept apart from the state: observing the state here would count as the screen observing it
    private val isRunning = MutableStateFlow(false)
    private var estimating: Job? = null

    // How far the clock of the server is ahead of the device: null until a first estimate
    private var clockOffset: Duration? = null

    // The call that asks the server to record the time up, and when its answer came, by the
    // estimated server time
    private var expiring: Job? = null
    private var lastExpiryAt: Instant? = null

    private var hasResumedOnce = false

    init {
        watch()
        estimateClock()
        tick()
    }

    private fun watch() {
        // The latest message of the chat, which the screen shows to open it
        watchingChat = viewModelScope.launch {
            watchMessagesUseCase(gameId = gameId)
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the chat" }
                }
                .collect { messages ->
                    _uiState.update { uiState ->
                        uiState.copy(lastMessage = messages.lastOrNull(), hasReadChat = true)
                    }
                }
        }
        watching = viewModelScope.launch {
            watchGameUseCase(gameId = gameId)
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the game" }
                }
                .collect { state ->
                    _uiState.update { uiState ->
                        uiState.copy(state = state)
                    }
                    isRunning.value = (state as? GameState.Available)?.game?.status == GameStatus.RUNNING
                    refreshRemaining()
                }
        }
    }

    /** Estimates the clock offset again: the device slept, or its clock may have been set. */
    fun onResumed() {
        // The first resume is the screen showing up, which init already estimates for
        if (hasResumedOnce) {
            estimateClock()
        }
        hasResumedOnce = true
    }

    private fun estimateClock() {
        estimating?.cancel()
        estimating = viewModelScope.launch {
            try {
                estimateServerClockOffsetUseCase().collect { offset ->
                    clockOffset = offset
                    refreshRemaining()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to estimate the clock of the server" }

                // Without the server, the clock of the device is the best guess left
                if (clockOffset == null) {
                    clockOffset = Duration.ZERO
                    refreshRemaining()
                }
            }
        }
    }

    /**
     * Computes the time left again and again, while the timer runs and the screen shows it: the
     * screen stops observing in the background, and the timer is computed again when it comes back.
     */
    private fun tick() {
        viewModelScope.launch {
            combine(
                _uiState.subscriptionCount.map { count -> count > 0 },
                isRunning,
            ) { isShown, isRunning -> isShown && isRunning }
                .distinctUntilChanged()
                .collectLatest { isTicking ->
                    while (isTicking) {
                        refreshRemaining()
                        delay(TICK)
                    }
                }
        }
    }

    private fun refreshRemaining() {
        val game = availableGame() ?: return
        val remaining = if (game.status == GameStatus.RUNNING) {
            val offset = clockOffset ?: return
            val serverNow = clock.now() + offset
            game.remainingAt(serverNow).also { remaining ->
                if (remaining == Duration.ZERO) expire(serverNow = serverNow)
            }
        } else {
            // Paused, waiting or over: the time left does not depend on the time
            game.remainingAt(clock.now())
        }
        _uiState.update { uiState ->
            uiState.copy(remaining = remaining.roundedUpToSeconds())
        }
    }

    // Asks the server to record the time up, again a little later while it says the game goes on:
    // the estimated clock may be slightly ahead of the server. Never while a call is under way: a
    // function waking up takes seconds, and calls piling up on one game slow each other down
    private fun expire(serverNow: Instant) {
        if (expiring?.isActive == true) return
        val lastExpiry = lastExpiryAt
        if (lastExpiry != null && serverNow - lastExpiry < EXPIRY_RETRY) return
        expiring = viewModelScope.launch {
            try {
                expireGameUseCase(gameId = gameId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to record the time up" }
            } finally {
                // The wait before trying again counts from the answer
                lastExpiryAt = clock.now() + (clockOffset ?: Duration.ZERO)
            }
        }
    }

    fun onPauseClicked() {
        runAction(action = PlayAction.PAUSE, failureMessage = "Failed to pause the game") {
            pauseGameUseCase(gameId = gameId)
        }
    }

    fun onResumeClicked() {
        runAction(action = PlayAction.RESUME, failureMessage = "Failed to resume the game") {
            resumeGameUseCase(gameId = gameId)
        }
    }

    fun onAddTimeConfirmed(minutes: Int) {
        // The minutes added so far, as the user saw them: a call sent again adds nothing twice
        val addedMinutes = availableGame()?.timer?.addedMinutes ?: return
        runAction(action = PlayAction.ADD_TIME, failureMessage = "Failed to add time") {
            addTimeUseCase(gameId = gameId, minutes = minutes, addedMinutes = addedMinutes)
        }
    }

    fun onEndConfirmed() {
        runAction(action = PlayAction.END, failureMessage = "Failed to end the game") {
            endGameUseCase(gameId = gameId)
        }
    }

    fun onKickConfirmed(playerId: String) {
        runAction(action = PlayAction.KICK, failureMessage = "Failed to exclude a player") {
            kickPlayerUseCase(gameId = gameId, playerId = playerId)
        }
    }

    fun onLeaveConfirmed() {
        close(action = PlayAction.LEAVE, failureMessage = "Failed to leave the game") {
            leaveGameUseCase(gameId = gameId)
        }
    }

    fun onDeleteConfirmed() {
        close(action = PlayAction.DELETE, failureMessage = "Failed to delete the game") {
            cancelGameUseCase(gameId = gameId)
        }
    }

    // One action at a time; what changed comes back through the game, which the screen observes
    private fun runAction(action: PlayAction, failureMessage: String, block: suspend () -> Unit) {
        if (_uiState.value.action != null) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(action = action)
            }
            try {
                block()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { failureMessage }

                _event.send(PlayEvent.ShowError(error = exception.toGameError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(action = null)
                }
                _event.send(PlayEvent.ActionFinished)
            }
        }
    }

    /**
     * Runs what takes the user out of the game, then closes the screen; tells a failure instead. The
     * game stops being observed first: an observation refused meanwhile would leave the Android SDK
     * late on the others (see docs/KMP/01_MODULE_DATA.md).
     */
    private fun close(action: PlayAction, failureMessage: String, block: suspend () -> Unit) {
        if (_uiState.value.action != null) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(action = action)
            }
            watching?.cancelAndJoin()
            watchingChat?.cancelAndJoin()
            try {
                block()

                _event.send(PlayEvent.Closed)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { failureMessage }

                watch()
                _event.send(PlayEvent.ShowError(error = exception.toGameError()))
                _event.send(PlayEvent.ActionFinished)
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(action = null)
                }
            }
        }
    }

    private fun availableGame(): Game? =
        (_uiState.value.state as? GameState.Available)?.game

    data class PlayUIState(
        // Null until the first read
        val state: GameState? = null,
        // The time left, in whole seconds rounded up; null while a running timer waits for the clock
        val remaining: Duration? = null,
        // The action under way, if any
        val action: PlayAction? = null,
        // The latest message of the chat, and whether the chat was read yet
        val lastMessage: ChatMessage? = null,
        val hasReadChat: Boolean = false,
    )

    enum class PlayAction {
        PAUSE,
        RESUME,
        ADD_TIME,
        END,
        KICK,
        LEAVE,
        DELETE,
    }

    sealed interface PlayEvent {
        data object Closed : PlayEvent

        /** An action is over, done or failed: its dialog closes. */
        data object ActionFinished : PlayEvent

        data class ShowError(val error: GameError) : PlayEvent
    }

    private companion object {
        // Shorter than a second: the timer changes second as soon as it should
        val TICK = 200.milliseconds
        val EXPIRY_RETRY = 2.seconds
    }
}

// A part of a second counts as a whole one: the timer shows 00:01 until it reaches zero
private fun Duration.roundedUpToSeconds(): Duration =
    ((inWholeMilliseconds + 999) / 1000).seconds
