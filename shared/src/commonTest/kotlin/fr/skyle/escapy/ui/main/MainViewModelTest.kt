package fr.skyle.escapy.ui.main

import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.fake.FakeWatchSessionUseCase
import fr.skyle.escapy.ui.main.MainViewModel.MainUIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val watchSessionUseCase = FakeWatchSessionUseCase()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the app waits for the first session`() = runTest {
        // Given
        val viewModel = MainViewModel(
            watchSessionUseCase = watchSessionUseCase,
        )

        // When
        val state = viewModel.uiState.value

        // Then
        assertEquals(
            expected = MainUIState.Loading,
            actual = state,
        )
    }

    @Test
    fun `the app follows the sign-in and the sign-out`() = runTest {
        // Given
        val viewModel = MainViewModel(
            watchSessionUseCase = watchSessionUseCase,
        )

        // When
        watchSessionUseCase.sessions.emit(
            Session.SignedIn(
                userId = "guest",
                isGuest = true,
            ),
        )
        val signedIn = viewModel.uiState.value
        watchSessionUseCase.sessions.emit(Session.SignedOut)
        val signedOut = viewModel.uiState.value

        // Then
        assertEquals(
            expected = MainUIState.Ready(isSignedIn = true),
            actual = signedIn,
        )
        assertEquals(
            expected = MainUIState.Ready(isSignedIn = false),
            actual = signedOut,
        )
    }

    @Test
    fun `a session that cannot be read is signed out`() = runTest {
        // Given
        watchSessionUseCase.failure = IllegalStateException("No session")

        // When
        val viewModel = MainViewModel(
            watchSessionUseCase = watchSessionUseCase,
        )

        // Then
        assertEquals(
            expected = MainUIState.Ready(isSignedIn = false),
            actual = viewModel.uiState.value,
        )
    }
}
