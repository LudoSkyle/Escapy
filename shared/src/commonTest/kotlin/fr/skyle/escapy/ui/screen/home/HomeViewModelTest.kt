package fr.skyle.escapy.ui.screen.home

import fr.skyle.escapy.domain.exception.GameNotFoundException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.fake.FakeJoinGameUseCase
import fr.skyle.escapy.fake.FakeWatchMyGamesUseCase
import fr.skyle.escapy.fake.game
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel.HomeEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeViewModelTest : MainDispatcherTest() {

    private val watchMyGamesUseCase = FakeWatchMyGamesUseCase()
    private val joinGameUseCase = FakeJoinGameUseCase()

    private val viewModel = HomeViewModel(
        watchMyGamesUseCase = watchMyGamesUseCase,
        joinGameUseCase = joinGameUseCase,
    )

    @Test
    fun `the games of the user are shown once read`() = runTest {
        // Given
        val loading = viewModel.uiState.value.games

        // When
        watchMyGamesUseCase.games.emit(listOf(game(isMaster = true)))

        // Then
        assertNull(loading)
        assertEquals(expected = listOf(game(isMaster = true)), actual = viewModel.uiState.value.games)
    }

    @Test
    fun `a joined game opens`() = runTest {
        // When
        viewModel.onJoinClicked(code = "ABC234")

        // Then
        assertEquals(expected = listOf("ABC234"), actual = joinGameUseCase.calls)
        assertEquals(expected = HomeEvent.OpenGame(gameId = "game"), actual = viewModel.event.first())
    }

    @Test
    fun `an incomplete code is not sent`() = runTest {
        // When
        viewModel.onJoinClicked(code = "ABC23")

        // Then
        assertTrue(joinGameUseCase.calls.isEmpty())
    }

    @Test
    fun `an unknown code is told under the field until it changes`() = runTest {
        // Given
        joinGameUseCase.failure = GameNotFoundException()

        // When
        viewModel.onJoinClicked(code = "ABC234")
        val error = viewModel.uiState.value.joinCodeError
        viewModel.onJoinCodeEdited()

        // Then
        assertEquals(expected = GameError.GAME_NOT_FOUND, actual = error)
        assertNull(viewModel.uiState.value.joinCodeError)
    }

    @Test
    fun `a network failure is told in a message`() = runTest {
        // Given
        joinGameUseCase.failure = NetworkUnavailableException()

        // When
        viewModel.onJoinClicked(code = "ABC234")

        // Then
        assertEquals(expected = HomeEvent.ShowError(error = GameError.NETWORK), actual = viewModel.event.first())
        assertNull(viewModel.uiState.value.joinCodeError)
    }
}
