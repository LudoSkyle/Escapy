package fr.skyle.escapy.ui.screen.home

import fr.skyle.escapy.domain.exception.GameNotFoundException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.model.Profile
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.fake.FakeJoinGameUseCase
import fr.skyle.escapy.fake.FakeWatchMyGamesUseCase
import fr.skyle.escapy.fake.FakeWatchProfileUseCase
import fr.skyle.escapy.fake.FakeWatchSessionUseCase
import fr.skyle.escapy.fake.gameSummary
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel.HomeEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : MainDispatcherTest() {

    private val watchMyGamesUseCase = FakeWatchMyGamesUseCase()
    private val joinGameUseCase = FakeJoinGameUseCase()
    private val watchSessionUseCase = FakeWatchSessionUseCase()
    private val watchProfileUseCase = FakeWatchProfileUseCase()

    private val viewModel = HomeViewModel(
        watchMyGamesUseCase = watchMyGamesUseCase,
        joinGameUseCase = joinGameUseCase,
        watchSessionUseCase = watchSessionUseCase,
        watchProfileUseCase = watchProfileUseCase,
    )

    @Test
    fun `the user is welcomed by their name once their profile is read`() = runTest {
        // Given
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val beforeProfile = viewModel.uiState.value.username

        // When
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "player", isGuest = true))
        watchProfileUseCase.profiles.emit(Profile(username = "Player_ABC234", avatarId = 7))

        // Then
        assertNull(beforeProfile)
        assertEquals(expected = "Player_ABC234", actual = viewModel.uiState.value.username)
    }

    @Test
    fun `the games of the user are shown once read`() = runTest {
        // Given
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val loading = viewModel.uiState.value.games

        // When
        watchMyGamesUseCase.games.emit(listOf(gameSummary(isMaster = true)))

        // Then
        assertNull(loading)
        assertEquals(expected = listOf(gameSummary(isMaster = true)), actual = viewModel.uiState.value.games)
    }

    @Test
    fun `the games are observed only while the screen is shown`() = runTest {
        // Given
        val before = watchMyGamesUseCase.games.subscriptionCount.value

        // When
        val shown = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val whileShown = watchMyGamesUseCase.games.subscriptionCount.value
        shown.cancel()

        // Then
        assertEquals(expected = 0, actual = before)
        assertEquals(expected = 1, actual = whileShown)
        assertEquals(expected = 0, actual = watchMyGamesUseCase.games.subscriptionCount.value)
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
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
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
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        joinGameUseCase.failure = NetworkUnavailableException()

        // When
        viewModel.onJoinClicked(code = "ABC234")

        // Then
        assertEquals(expected = HomeEvent.ShowError(error = GameError.NETWORK), actual = viewModel.event.first())
        assertNull(viewModel.uiState.value.joinCodeError)
    }
}
