package fr.skyle.escapy.ui.screen.deleteaccount

import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.domain.exception.WrongGoogleAccountException
import fr.skyle.escapy.domain.model.GoogleTokens
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.fake.FakeDeleteAccountUseCase
import fr.skyle.escapy.fake.FakeWatchSessionUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.deleteaccount.ui.DeleteAccountViewModel
import fr.skyle.escapy.ui.screen.deleteaccount.ui.DeleteAccountViewModel.DeleteAccountEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteAccountViewModelTest : MainDispatcherTest() {

    private val watchSessionUseCase = FakeWatchSessionUseCase()
    private val deleteAccountUseCase = FakeDeleteAccountUseCase()

    private val viewModel = DeleteAccountViewModel(
        watchSessionUseCase = watchSessionUseCase,
        deleteAccountUseCase = deleteAccountUseCase,
    )

    @Test
    fun `a member confirms with their password before the deletion`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "member", isGuest = false, email = "member@escapy.app", hasPassword = true))

        // When
        viewModel.onDeleteClicked(currentPassword = "")
        val askedWithoutPassword = viewModel.uiState.value.isConfirmationShown
        viewModel.onDeleteClicked(currentPassword = "Escapy-2026")
        val asked = viewModel.uiState.value.isConfirmationShown
        viewModel.onDeletionConfirmed(currentPassword = "Escapy-2026")

        // Then
        assertFalse(askedWithoutPassword)
        assertTrue(asked)
        assertEquals(
            expected = listOf("Escapy-2026"),
            actual = deleteAccountUseCase.calls,
        )
    }

    @Test
    fun `a guest is deleted without a password`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "guest", isGuest = true))

        // When
        viewModel.onDeleteClicked(currentPassword = "")
        viewModel.onDeletionConfirmed(currentPassword = "")

        // Then
        assertEquals(
            expected = listOf("guest"),
            actual = deleteAccountUseCase.calls,
        )
    }

    @Test
    fun `a wrong password is shown under it`() = runTest {
        // Given
        deleteAccountUseCase.failure = InvalidCredentialsException()
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "member", isGuest = false, email = "member@escapy.app", hasPassword = true))

        // When
        viewModel.onDeleteClicked(currentPassword = "Wrong-2026")
        viewModel.onDeletionConfirmed(currentPassword = "Wrong-2026")

        // Then
        assertEquals(
            expected = AccountError.INVALID_CREDENTIALS,
            actual = viewModel.uiState.value.passwordError,
        )
    }

    @Test
    fun `a member of Google confirms with Google without a password`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(googleMember())

        // When
        viewModel.onDeleteClicked(currentPassword = "")
        val asked = viewModel.uiState.value.isConfirmationShown
        viewModel.onDeletionConfirmed(currentPassword = "")
        val deletedWithoutToken = deleteAccountUseCase.calls.toList()
        viewModel.onGoogleTokensReceived(tokens = GoogleTokens(idToken = "token", accessToken = null))

        // Then
        assertTrue(asked)
        assertEquals(
            expected = emptyList(),
            actual = deletedWithoutToken,
        )
        assertEquals(
            expected = listOf("google:token"),
            actual = deleteAccountUseCase.calls,
        )
    }

    @Test
    fun `another Google account is told by a message`() = runTest {
        // Given
        deleteAccountUseCase.failure = WrongGoogleAccountException()
        watchSessionUseCase.sessions.emit(googleMember())

        // When
        viewModel.onDeleteClicked(currentPassword = "")
        viewModel.onGoogleTokensReceived(tokens = GoogleTokens(idToken = "token", accessToken = null))

        // Then
        assertEquals(
            expected = DeleteAccountEvent.ShowError(error = AccountError.WRONG_GOOGLE_ACCOUNT),
            actual = viewModel.event.first(),
        )
        assertFalse(viewModel.uiState.value.isConfirmationShown)
    }

    @Test
    fun `a Google sign-in that failed closes the confirmation`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(googleMember())
        viewModel.onDeleteClicked(currentPassword = "")

        // When
        viewModel.onGoogleSignInFailed(exception = IllegalStateException("No token"))

        // Then
        assertFalse(viewModel.uiState.value.isConfirmationShown)
        assertEquals(
            expected = DeleteAccountEvent.ShowError(error = AccountError.UNKNOWN),
            actual = viewModel.event.first(),
        )
    }

    private fun googleMember() =
        Session.SignedIn(userId = "member", isGuest = false, email = "member@gmail.com", hasPassword = false)
}
