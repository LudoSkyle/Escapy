package fr.skyle.escapy.ui.screen.profile

import fr.skyle.escapy.domain.model.Profile
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.fake.FakeDeleteAccountUseCase
import fr.skyle.escapy.fake.FakeSignOutUseCase
import fr.skyle.escapy.fake.FakeWatchProfileUseCase
import fr.skyle.escapy.fake.FakeWatchSessionUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.screen.profile.ui.ProfileViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileViewModelTest : MainDispatcherTest() {

    private val watchSessionUseCase = FakeWatchSessionUseCase()
    private val watchProfileUseCase = FakeWatchProfileUseCase()
    private val signOutUseCase = FakeSignOutUseCase()
    private val deleteAccountUseCase = FakeDeleteAccountUseCase()

    private val viewModel = ProfileViewModel(
        watchSessionUseCase = watchSessionUseCase,
        watchProfileUseCase = watchProfileUseCase,
        signOutUseCase = signOutUseCase,
        deleteAccountUseCase = deleteAccountUseCase,
    )

    @Test
    fun `the profile and the email of the session are shown`() = runTest {
        // When
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "member", isGuest = false, email = "member@escapy.app"))
        watchProfileUseCase.profiles.emit(Profile(username = "Player_ABC234", avatarId = 7))

        // Then
        assertEquals(
            expected = Profile(username = "Player_ABC234", avatarId = 7),
            actual = viewModel.uiState.value.profile,
        )
        assertEquals(
            expected = "member@escapy.app",
            actual = viewModel.uiState.value.email,
        )
    }

    @Test
    fun `a member signs out without deleting anything`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "member", isGuest = false, email = "member@escapy.app"))

        // When
        viewModel.onSignOutClicked()

        // Then
        assertEquals(
            expected = listOf("signOut"),
            actual = signOutUseCase.calls,
        )
        assertEquals(
            expected = emptyList(),
            actual = deleteAccountUseCase.calls,
        )
    }

    @Test
    fun `a guest confirms then their account is deleted rather than left on the server`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "guest", isGuest = true))

        // When
        viewModel.onSignOutClicked()
        val isDialogShown = viewModel.uiState.value.isGuestSignOutDialogShown
        viewModel.onGuestSignOutConfirmed()

        // Then
        assertTrue(isDialogShown)
        assertEquals(
            expected = listOf("guest"),
            actual = deleteAccountUseCase.calls,
        )
        assertEquals(
            expected = emptyList(),
            actual = signOutUseCase.calls,
        )
    }
}
