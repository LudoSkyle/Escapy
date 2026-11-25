package fr.skyle.escapy.ui.main

import fr.skyle.escapy.domain.model.Profile
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.fake.FakeEnsureProfileUseCase
import fr.skyle.escapy.fake.FakeSignOutUseCase
import fr.skyle.escapy.fake.FakeWatchProfileUseCase
import fr.skyle.escapy.fake.FakeWatchSessionUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.main.MainViewModel.MainUIState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MainViewModelTest : MainDispatcherTest() {

    private val watchSessionUseCase = FakeWatchSessionUseCase()
    private val watchProfileUseCase = FakeWatchProfileUseCase()
    private val ensureProfileUseCase = FakeEnsureProfileUseCase()
    private val signOutUseCase = FakeSignOutUseCase()

    private val viewModel = MainViewModel(
        watchSessionUseCase = watchSessionUseCase,
        watchProfileUseCase = watchProfileUseCase,
        ensureProfileUseCase = ensureProfileUseCase,
        signOutUseCase = signOutUseCase,
    )

    private val profile = Profile(
        username = "Player_ABC234",
        avatarId = 7,
    )

    @Test
    fun `the app waits for the first session`() {
        assertEquals(
            expected = MainUIState.Loading,
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `without a session the app is signed out`() = runTest {
        // When
        watchSessionUseCase.sessions.emit(Session.SignedOut)

        // Then
        assertEquals(
            expected = MainUIState.SignedOut,
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `a session with its profile in the cache is signed in without calling the server`() = runTest {
        // When
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "user", isGuest = false))
        val waiting = viewModel.uiState.value
        watchProfileUseCase.profiles.emit(profile)

        // Then
        assertEquals(
            expected = MainUIState.Loading,
            actual = waiting,
        )
        assertEquals(
            expected = MainUIState.SignedIn,
            actual = viewModel.uiState.value,
        )
        assertEquals(
            expected = emptyList(),
            actual = ensureProfileUseCase.calls,
        )
    }

    @Test
    fun `a missing profile is created once then the app is signed in`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "user", isGuest = true))

        // When
        watchProfileUseCase.profiles.emit(null)
        watchProfileUseCase.profiles.emit(null)
        watchProfileUseCase.profiles.emit(profile)

        // Then
        assertEquals(
            expected = listOf("ensure"),
            actual = ensureProfileUseCase.calls,
        )
        assertEquals(
            expected = MainUIState.SignedIn,
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `a profile deleted after it was read is not created again`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "user", isGuest = false))
        watchProfileUseCase.profiles.emit(profile)

        // When
        watchProfileUseCase.profiles.emit(null)

        // Then
        assertEquals(
            expected = emptyList(),
            actual = ensureProfileUseCase.calls,
        )
        assertEquals(
            expected = MainUIState.SignedIn,
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `a profile that cannot be created makes it unavailable until a retry`() = runTest {
        // Given
        ensureProfileUseCase.failure = IllegalStateException("No network")
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "user", isGuest = false))
        watchProfileUseCase.profiles.emit(null)
        val unavailable = viewModel.uiState.value

        // When
        ensureProfileUseCase.failure = null
        viewModel.onRetryClicked()
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "user", isGuest = false))
        watchProfileUseCase.profiles.emit(null)
        watchProfileUseCase.profiles.emit(profile)

        // Then
        assertEquals(
            expected = MainUIState.ProfileUnavailable,
            actual = unavailable,
        )
        assertEquals(
            expected = listOf("ensure", "ensure"),
            actual = ensureProfileUseCase.calls,
        )
        assertEquals(
            expected = MainUIState.SignedIn,
            actual = viewModel.uiState.value,
        )
    }

    @Test
    fun `a session that cannot be read is signed out`() = runTest {
        // Given
        watchSessionUseCase.failure = IllegalStateException("No session")

        // When
        val failingViewModel = MainViewModel(
            watchSessionUseCase = watchSessionUseCase,
            watchProfileUseCase = watchProfileUseCase,
            ensureProfileUseCase = ensureProfileUseCase,
            signOutUseCase = signOutUseCase,
        )

        // Then
        assertEquals(
            expected = MainUIState.SignedOut,
            actual = failingViewModel.uiState.value,
        )
    }

    @Test
    fun `the user can sign out from an unavailable profile`() = runTest {
        // When
        viewModel.onSignOutClicked()

        // Then
        assertEquals(
            expected = listOf("signOut"),
            actual = signOutUseCase.calls,
        )
    }
}
