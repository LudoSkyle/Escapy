package fr.skyle.escapy.ui.screen.creategame

import fr.skyle.escapy.domain.exception.InvalidGameTitleException
import fr.skyle.escapy.domain.exception.TooManyGamesException
import fr.skyle.escapy.fake.FakeCreateGameUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.screen.creategame.ui.CreateGameViewModel
import fr.skyle.escapy.ui.screen.creategame.ui.CreateGameViewModel.CreateGameEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CreateGameViewModelTest : MainDispatcherTest() {

    private val createGameUseCase = FakeCreateGameUseCase()

    private val viewModel = CreateGameViewModel(
        createGameUseCase = createGameUseCase,
    )

    @Test
    fun `a game is created with the duration and the maximum chosen then opens`() = runTest {
        // Given
        viewModel.onDurationIncreased()
        viewModel.onMaxPlayersIncreased()
        viewModel.onMaxPlayersIncreased()

        // When
        viewModel.onCreateClicked(title = "Le manoir")

        // Then
        assertEquals(expected = listOf("Le manoir:65:3"), actual = createGameUseCase.calls)
        assertEquals(expected = CreateGameEvent.Created(gameId = "game"), actual = viewModel.event.first())
    }

    @Test
    fun `the duration stays between 15 minutes and 3 hours by steps of 5`() = runTest {
        // When
        repeat(times = 20) { viewModel.onDurationDecreased() }
        val shortest = viewModel.uiState.value
        repeat(times = 50) { viewModel.onDurationIncreased() }
        val longest = viewModel.uiState.value

        // Then
        assertEquals(expected = 15, actual = shortest.durationMinutes)
        assertFalse(shortest.canShorten)
        assertEquals(expected = 180, actual = longest.durationMinutes)
        assertFalse(longest.canLengthen)
    }

    @Test
    fun `the maximum goes from none to 2 then up to 20 and back to none`() = runTest {
        // Given
        val none = viewModel.uiState.value.maxPlayers

        // When
        viewModel.onMaxPlayersIncreased()
        val lowest = viewModel.uiState.value.maxPlayers
        repeat(times = 30) { viewModel.onMaxPlayersIncreased() }
        val highest = viewModel.uiState.value
        repeat(times = 30) { viewModel.onMaxPlayersDecreased() }

        // Then
        assertNull(none)
        assertEquals(expected = 2, actual = lowest)
        assertEquals(expected = 20, actual = highest.maxPlayers)
        assertFalse(highest.canRaiseMaxPlayers)
        assertNull(viewModel.uiState.value.maxPlayers)
        assertFalse(viewModel.uiState.value.canLowerMaxPlayers)
    }

    @Test
    fun `a title refused by the server is told under the field until it changes`() = runTest {
        // Given
        createGameUseCase.failure = InvalidGameTitleException()

        // When
        viewModel.onCreateClicked(title = "Ab")
        val error = viewModel.uiState.value.titleError
        viewModel.onTitleEdited()

        // Then
        assertEquals(expected = GameError.INVALID_GAME_TITLE, actual = error)
        assertNull(viewModel.uiState.value.titleError)
    }

    @Test
    fun `a sixth game is told in a message`() = runTest {
        // Given
        createGameUseCase.failure = TooManyGamesException()

        // When
        viewModel.onCreateClicked(title = "Le manoir")

        // Then
        assertEquals(expected = CreateGameEvent.ShowError(error = GameError.TOO_MANY_GAMES), actual = viewModel.event.first())
    }
}
