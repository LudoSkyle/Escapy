package fr.skyle.escapy.ui.screen.waitingroom

import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.MissionState
import fr.skyle.escapy.fake.FakeCancelGameUseCase
import fr.skyle.escapy.fake.FakeKickPlayerUseCase
import fr.skyle.escapy.fake.FakeLeaveGameUseCase
import fr.skyle.escapy.fake.FakeStartGameUseCase
import fr.skyle.escapy.fake.FakeWatchGameUseCase
import fr.skyle.escapy.fake.FakeWatchMissionsUseCase
import fr.skyle.escapy.fake.mission
import fr.skyle.escapy.fake.game
import fr.skyle.escapy.fake.gameMember
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.screen.waitingroom.ui.WaitingRoomViewModel
import fr.skyle.escapy.ui.screen.waitingroom.ui.WaitingRoomViewModel.WaitingRoomEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WaitingRoomViewModelTest : MainDispatcherTest() {

    private val watchGameUseCase = FakeWatchGameUseCase()
    private val leaveGameUseCase = FakeLeaveGameUseCase()
    private val kickPlayerUseCase = FakeKickPlayerUseCase()
    private val cancelGameUseCase = FakeCancelGameUseCase()

    private val startGameUseCase = FakeStartGameUseCase()
    private val watchMissionsUseCase = FakeWatchMissionsUseCase()

    private val viewModel = WaitingRoomViewModel(
        gameId = "game",
        watchGameUseCase = watchGameUseCase,
        leaveGameUseCase = leaveGameUseCase,
        kickPlayerUseCase = kickPlayerUseCase,
        cancelGameUseCase = cancelGameUseCase,
        startGameUseCase = startGameUseCase,
        watchMissionsUseCase = watchMissionsUseCase,
    )

    private val player = gameMember(userId = "player")

    @Test
    fun `the game and its players are shown live`() = runTest {
        // Given
        val available = GameState.Available(game = game(isMaster = true), players = listOf(player))

        // When
        watchGameUseCase.states.emit(available)

        // Then
        assertEquals(expected = available, actual = viewModel.uiState.value.state)
    }

    @Test
    fun `a game cancelled by the game master is told to a player`() = runTest {
        // When
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = false), players = listOf(player)))
        watchGameUseCase.states.emit(GameState.Deleted)

        // Then
        assertEquals(expected = GameState.Deleted, actual = viewModel.uiState.value.state)
    }

    @Test
    fun `an exclusion confirmed reaches the server and closes the dialog`() = runTest {
        // Given
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true), players = listOf(player)))

        // When
        viewModel.onKickConfirmed(playerId = player.userId)

        // Then
        assertEquals(expected = listOf("game:player"), actual = kickPlayerUseCase.calls)
        assertEquals(expected = WaitingRoomEvent.ActionFinished, actual = viewModel.event.first())
        assertFalse(viewModel.uiState.value.isKicking)
    }

    @Test
    fun `a player who leaves stops observing the game then closes the screen`() = runTest {
        // Given
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = false), players = listOf(player)))
        val gate = CompletableDeferred<Unit>()
        leaveGameUseCase.gate = gate

        // When
        viewModel.onLeaveConfirmed()
        val observedWhileLeaving = watchGameUseCase.states.subscriptionCount.value
        gate.complete(Unit)

        // Then
        assertEquals(expected = 0, actual = observedWhileLeaving)
        assertEquals(expected = listOf("game"), actual = leaveGameUseCase.calls)
        assertEquals(expected = WaitingRoomEvent.Closed, actual = viewModel.event.first())
        assertTrue(viewModel.uiState.value.state is GameState.Available)
    }

    @Test
    fun `the game master who cancels closes the screen`() = runTest {
        // Given
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true), players = emptyList()))

        // When
        viewModel.onCancelConfirmed()
        watchGameUseCase.states.emit(GameState.Deleted)

        // Then
        assertEquals(expected = listOf("game"), actual = cancelGameUseCase.calls)
        assertEquals(expected = WaitingRoomEvent.Closed, actual = viewModel.event.first())
    }

    @Test
    fun `a failure to cancel is told closes the dialog and keeps the screen`() = runTest {
        // Given
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true), players = emptyList()))
        cancelGameUseCase.failure = NetworkUnavailableException()

        // When
        viewModel.onCancelConfirmed()

        // Then
        assertEquals(
            expected = listOf(WaitingRoomEvent.ShowError(error = GameError.NETWORK), WaitingRoomEvent.ActionFinished),
            actual = viewModel.event.take(2).toList(),
        )
        assertFalse(viewModel.uiState.value.isCancelling)
        assertEquals(expected = 1, actual = watchGameUseCase.states.subscriptionCount.value)
    }

    @Test
    fun `the game master starts the game and the screen gives way to it once`() = runTest {
        // Given
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true), players = listOf(player)))

        // When
        viewModel.onStartClicked()
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true, status = GameStatus.RUNNING), players = listOf(player)))
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true, status = GameStatus.PAUSED), players = listOf(player)))

        // Then
        assertEquals(expected = listOf("game"), actual = startGameUseCase.calls)
        assertFalse(viewModel.uiState.value.isStarting)
        assertEquals(expected = WaitingRoomEvent.Started, actual = viewModel.event.first())
        assertNull(withTimeoutOrNull(1_000) { viewModel.event.first() })
    }

    @Test
    fun `a player sees the game start`() = runTest {
        // When
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = false, status = GameStatus.RUNNING), players = listOf(player)))

        // Then
        assertEquals(expected = WaitingRoomEvent.Started, actual = viewModel.event.first())
    }

    @Test
    fun `the game master sees the missions they prepare and a player observes none`() = runTest {
        // When
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true), players = emptyList()))
        watchMissionsUseCase.missions.emit(listOf(mission(id = "safe", state = MissionState.LOCKED)))

        // Then
        assertEquals(expected = listOf(true), actual = watchMissionsUseCase.asMaster)
        assertEquals(expected = listOf("safe"), actual = viewModel.uiState.value.missions?.map { mission -> mission.id })
    }

    @Test
    fun `a player observes no mission in the waiting room`() = runTest {
        // When
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = false), players = listOf(player)))

        // Then
        assertTrue(watchMissionsUseCase.asMaster.isEmpty())
    }
}
