package fr.skyle.escapy.ui.main

import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_followsTheSession() = runTest {
        // Given
        val sessions = MutableSharedFlow<Session>()
        val viewModel = MainViewModel(watchSessionUseCase = FakeWatchSessionUseCase(sessions))
        val states = mutableListOf<MainUiState>()
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect(states::add) }

        // When
        sessions.emit(Session.SignedOut)
        sessions.emit(Session.SignedIn(userId = "guest", isGuest = true))
        sessions.emit(Session.SignedOut)

        // Then
        assertEquals(
            listOf(
                MainUiState.Loading,
                MainUiState.Ready(isSignedIn = false),
                MainUiState.Ready(isSignedIn = true),
                MainUiState.Ready(isSignedIn = false),
            ),
            states,
        )
        collector.cancel()
    }

    @Test
    fun uiState_whenTheSessionFails_isSignedOut() = runTest {
        // Given
        val failing = flow<Session> { throw IllegalStateException("No session") }
        val viewModel = MainViewModel(watchSessionUseCase = FakeWatchSessionUseCase(failing))
        val states = mutableListOf<MainUiState>()

        // When
        val collector = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect(states::add) }

        // Then
        assertEquals(MainUiState.Ready(isSignedIn = false), states.last())
        collector.cancel()
    }

    private class FakeWatchSessionUseCase(
        private val sessions: Flow<Session>,
    ) : WatchSessionUseCase {
        override fun invoke(): Flow<Session> = sessions
    }
}
