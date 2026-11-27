package fr.skyle.escapy.ui.screen.play

import androidx.lifecycle.viewModelScope
import fr.skyle.escapy.domain.exception.GameFinishedException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.GameTimer
import fr.skyle.escapy.fake.FakeAddTimeUseCase
import fr.skyle.escapy.fake.FakeCancelGameUseCase
import fr.skyle.escapy.fake.FakeEndGameUseCase
import fr.skyle.escapy.fake.FakeEstimateServerClockOffsetUseCase
import fr.skyle.escapy.fake.FakeExpireGameUseCase
import fr.skyle.escapy.fake.FakeKickPlayerUseCase
import fr.skyle.escapy.fake.FakeLeaveGameUseCase
import fr.skyle.escapy.fake.FakePauseGameUseCase
import fr.skyle.escapy.fake.FakeResumeGameUseCase
import fr.skyle.escapy.fake.FakeWatchGameUseCase
import fr.skyle.escapy.fake.FakeWatchMessagesUseCase
import fr.skyle.escapy.fake.chatMessage
import fr.skyle.escapy.fake.game
import fr.skyle.escapy.fake.gameMember
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.screen.play.ui.PlayViewModel
import fr.skyle.escapy.ui.screen.play.ui.PlayViewModel.PlayEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PlayViewModelTest : MainDispatcherTest() {

    private val watchGameUseCase = FakeWatchGameUseCase()
    private val watchMessagesUseCase = FakeWatchMessagesUseCase()
    private val estimateServerClockOffsetUseCase = FakeEstimateServerClockOffsetUseCase()
    private val pauseGameUseCase = FakePauseGameUseCase()
    private val addTimeUseCase = FakeAddTimeUseCase()
    private val expireGameUseCase = FakeExpireGameUseCase()
    private val leaveGameUseCase = FakeLeaveGameUseCase()

    // The device clock moves with the virtual time of the test, as delay() does
    private val clock = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(START_MS + mainScheduler.currentTime)
    }

    private val player = gameMember(userId = "player")

    private val viewModels = mutableListOf<PlayViewModel>()

    private fun viewModel() =
        PlayViewModel(
            gameId = "game",
            watchGameUseCase = watchGameUseCase,
            watchMessagesUseCase = watchMessagesUseCase,
            estimateServerClockOffsetUseCase = estimateServerClockOffsetUseCase,
            pauseGameUseCase = pauseGameUseCase,
            resumeGameUseCase = FakeResumeGameUseCase(),
            addTimeUseCase = addTimeUseCase,
            endGameUseCase = FakeEndGameUseCase(),
            expireGameUseCase = expireGameUseCase,
            kickPlayerUseCase = FakeKickPlayerUseCase(),
            leaveGameUseCase = leaveGameUseCase,
            cancelGameUseCase = FakeCancelGameUseCase(),
            clock = clock,
        ).also { viewModel -> viewModels += viewModel }

    /**
     * A test whose ViewModels stop at its end, as a screen closing clears them: their timer would keep
     * the virtual time going when runTest runs what is left.
     */
    private fun playTest(body: suspend TestScope.() -> Unit) = runTest {
        try {
            body()
        } finally {
            viewModels.forEach { viewModel -> viewModel.viewModelScope.cancel() }
        }
    }

    /** A running game that ends [endsIn] after the start, by the clock of the server. */
    private fun running(endsIn: Duration, serverAhead: Duration = Duration.ZERO, addedMinutes: Int = 0, isMaster: Boolean = true) =
        GameState.Available(
            game = game(
                isMaster = isMaster,
                status = GameStatus.RUNNING,
                timer = GameTimer(
                    endsAt = Instant.fromEpochMilliseconds(START_MS) + serverAhead + endsIn,
                    remaining = null,
                    addedMinutes = addedMinutes,
                ),
            ),
            players = listOf(player),
        )

    @Test
    fun `the timer counts down by the clock of the server`() = playTest {
        // Given: the server is 19 seconds ahead of the device
        estimateServerClockOffsetUseCase.offsets = listOf(19.seconds)
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }

        // When
        watchGameUseCase.states.emit(running(endsIn = 10.minutes, serverAhead = 19.seconds))
        val atStart = viewModel.uiState.value.remaining
        advanceTimeBy(3_100)

        // Then
        assertEquals(expected = 10.minutes, actual = atStart)
        assertEquals(expected = 10.minutes - 3.seconds, actual = viewModel.uiState.value.remaining)
    }

    @Test
    fun `a running timer waits for the clock`() = playTest {
        // Given: no estimate comes yet
        estimateServerClockOffsetUseCase.offsets = emptyList()
        val viewModel = viewModel()

        // When
        watchGameUseCase.states.emit(running(endsIn = 10.minutes))

        // Then
        assertNull(viewModel.uiState.value.remaining)
    }

    @Test
    fun `without the server the timer falls back on the device clock`() = playTest {
        // Given
        estimateServerClockOffsetUseCase.offsets = emptyList()
        estimateServerClockOffsetUseCase.failure = NetworkUnavailableException()
        val viewModel = viewModel()

        // When
        watchGameUseCase.states.emit(running(endsIn = 10.minutes))

        // Then
        assertEquals(expected = 10.minutes, actual = viewModel.uiState.value.remaining)
    }

    @Test
    fun `a paused game shows the time it kept without waiting for the clock`() = playTest {
        // Given: no estimate ever comes
        estimateServerClockOffsetUseCase.offsets = emptyList()
        val viewModel = viewModel()

        // When
        watchGameUseCase.states.emit(
            GameState.Available(
                game = game(isMaster = false, status = GameStatus.PAUSED, timer = GameTimer(endsAt = null, remaining = 90.seconds, addedMinutes = 0)),
                players = listOf(player),
            ),
        )
        advanceTimeBy(5_000)

        // Then
        assertEquals(expected = 90.seconds, actual = viewModel.uiState.value.remaining)
    }

    @Test
    fun `at zero the server is asked to record the time up then again a little later only`() = playTest {
        // Given
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        watchGameUseCase.states.emit(running(endsIn = 2.seconds))

        // When
        advanceTimeBy(2_100)
        val callsAtZero = expireGameUseCase.calls.size
        advanceTimeBy(1_000)
        val callsSoonAfter = expireGameUseCase.calls.size
        advanceTimeBy(1_500)

        // Then
        assertEquals(expected = Duration.ZERO, actual = viewModel.uiState.value.remaining)
        assertEquals(expected = 1, actual = callsAtZero)
        assertEquals(expected = 1, actual = callsSoonAfter)
        assertEquals(expected = 2, actual = expireGameUseCase.calls.size)
    }

    @Test
    fun `no other call asks the time up while one is under way`() = playTest {
        // Given: the function takes its time to wake up
        val gate = CompletableDeferred<Unit>()
        expireGameUseCase.gate = gate
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        watchGameUseCase.states.emit(running(endsIn = 1.seconds))

        // When
        advanceTimeBy(6_000)
        val callsWhileWaiting = expireGameUseCase.calls.size
        gate.complete(Unit)
        advanceTimeBy(1_000)
        val callsSoonAfterTheAnswer = expireGameUseCase.calls.size
        advanceTimeBy(1_500)

        // Then
        assertEquals(expected = 1, actual = callsWhileWaiting)
        assertEquals(expected = 1, actual = callsSoonAfterTheAnswer)
        assertEquals(expected = 2, actual = expireGameUseCase.calls.size)
    }

    @Test
    fun `adding time sends the minutes already added as the user saw them`() = playTest {
        // Given
        val viewModel = viewModel()
        watchGameUseCase.states.emit(running(endsIn = 10.minutes, addedMinutes = 5))

        // When
        viewModel.onAddTimeConfirmed(minutes = 10)

        // Then
        assertEquals(expected = listOf("game:10:5"), actual = addTimeUseCase.calls)
        assertEquals(expected = PlayEvent.ActionFinished, actual = viewModel.event.first())
        assertNull(viewModel.uiState.value.action)
    }

    @Test
    fun `a failure to pause is told and closes nothing`() = playTest {
        // Given
        val viewModel = viewModel()
        watchGameUseCase.states.emit(running(endsIn = 10.minutes))
        pauseGameUseCase.failure = GameFinishedException()

        // When
        viewModel.onPauseClicked()

        // Then
        assertEquals(
            expected = listOf(PlayEvent.ShowError(error = GameError.GAME_FINISHED), PlayEvent.ActionFinished),
            actual = viewModel.event.take(2).toList(),
        )
    }

    @Test
    fun `a player who leaves stops observing the game then closes the screen`() = playTest {
        // Given
        val viewModel = viewModel()
        watchGameUseCase.states.emit(running(endsIn = 10.minutes, isMaster = false))
        val gate = CompletableDeferred<Unit>()
        leaveGameUseCase.gate = gate

        // When
        viewModel.onLeaveConfirmed()
        val observedWhileLeaving = watchGameUseCase.states.subscriptionCount.value
        val chatObservedWhileLeaving = watchMessagesUseCase.messages.subscriptionCount.value
        gate.complete(Unit)

        // Then
        assertEquals(expected = 0, actual = observedWhileLeaving)
        assertEquals(expected = 0, actual = chatObservedWhileLeaving)
        assertEquals(expected = listOf("game"), actual = leaveGameUseCase.calls)
        assertEquals(expected = PlayEvent.Closed, actual = viewModel.event.first())
    }

    @Test
    fun `the timer is not computed again while the screen does not show it`() = playTest {
        // Given: nobody observes the state
        val viewModel = viewModel()
        watchGameUseCase.states.emit(running(endsIn = 2.seconds))

        // When
        advanceTimeBy(5_000)

        // Then
        assertEquals(expected = 2.seconds, actual = viewModel.uiState.value.remaining)
        assertEquals(expected = 0, actual = expireGameUseCase.calls.size)
    }

    @Test
    fun `the latest message of the chat is shown and a chat without any is told so`() = playTest {
        // Given
        val viewModel = viewModel()

        // When
        watchMessagesUseCase.messages.emit(emptyList())
        val beforeAnyMessage = viewModel.uiState.value
        watchMessagesUseCase.messages.emit(listOf(chatMessage(id = "1"), chatMessage(id = "2", text = "Regardez sous le tapis")))

        // Then
        assertTrue(beforeAnyMessage.hasReadChat)
        assertNull(beforeAnyMessage.lastMessage)
        assertEquals(expected = "Regardez sous le tapis", actual = viewModel.uiState.value.lastMessage?.text)
    }

    @Test
    fun `coming back to the screen estimates the clock again but its first showing does not`() = playTest {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.onResumed()
        val callsAfterShowing = estimateServerClockOffsetUseCase.calls
        viewModel.onResumed()

        // Then
        assertEquals(expected = 1, actual = callsAfterShowing)
        assertEquals(expected = 2, actual = estimateServerClockOffsetUseCase.calls)
    }

    private companion object {
        const val START_MS = 1_000_000_000L
    }
}
