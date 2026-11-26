package fr.skyle.escapy.ui.screen.waitingroom

import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.fake.FakeCancelGameUseCase
import fr.skyle.escapy.fake.FakeKickPlayerUseCase
import fr.skyle.escapy.fake.FakeLeaveGameUseCase
import fr.skyle.escapy.fake.FakeWatchGameUseCase
import fr.skyle.escapy.fake.game
import fr.skyle.escapy.fake.gameMember
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.screen.waitingroom.ui.WaitingRoomViewModel
import fr.skyle.escapy.ui.screen.waitingroom.ui.WaitingRoomViewModel.WaitingRoomEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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

    private val viewModel = WaitingRoomViewModel(
        gameId = "game",
        watchGameUseCase = watchGameUseCase,
        leaveGameUseCase = leaveGameUseCase,
        kickPlayerUseCase = kickPlayerUseCase,
        cancelGameUseCase = cancelGameUseCase,
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
    fun `the game master excludes a player after confirming`() = runTest {
        // Given
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true), players = listOf(player)))

        // When
        viewModel.onKickClicked(player = player)
        val asked = viewModel.uiState.value.playerToKick
        viewModel.onKickConfirmed()

        // Then
        assertEquals(expected = player, actual = asked)
        assertEquals(expected = listOf("game:player"), actual = kickPlayerUseCase.calls)
        assertNull(viewModel.uiState.value.playerToKick)
    }

    @Test
    fun `a player who leaves closes the screen without being told they are out`() = runTest {
        // Given
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = false), players = listOf(player)))
        val gate = CompletableDeferred<Unit>()
        leaveGameUseCase.gate = gate

        // When
        viewModel.onLeaveClicked()
        viewModel.onLeaveConfirmed()
        // The game is no longer readable before the call returns
        watchGameUseCase.states.emit(GameState.Excluded)
        gate.complete(Unit)

        // Then
        assertEquals(expected = listOf("game"), actual = leaveGameUseCase.calls)
        assertEquals(expected = WaitingRoomEvent.Closed, actual = viewModel.event.first())
        assertTrue(viewModel.uiState.value.state is GameState.Available)
        assertFalse(viewModel.uiState.value.isLeaveDialogShown)
    }

    @Test
    fun `the game master who cancels closes the screen`() = runTest {
        // Given
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true), players = emptyList()))

        // When
        viewModel.onCancelClicked()
        viewModel.onCancelConfirmed()
        watchGameUseCase.states.emit(GameState.Deleted)

        // Then
        assertEquals(expected = listOf("game"), actual = cancelGameUseCase.calls)
        assertEquals(expected = WaitingRoomEvent.Closed, actual = viewModel.event.first())
    }

    @Test
    fun `a failure to cancel is told and keeps the screen`() = runTest {
        // Given
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = true), players = emptyList()))
        cancelGameUseCase.failure = NetworkUnavailableException()

        // When
        viewModel.onCancelClicked()
        viewModel.onCancelConfirmed()

        // Then
        assertEquals(expected = WaitingRoomEvent.ShowError(error = GameError.NETWORK), actual = viewModel.event.first())
        assertFalse(viewModel.uiState.value.isCancelling)
    }
}
