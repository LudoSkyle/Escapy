package fr.skyle.escapy.ui.screen.deleteaccount

import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.fake.FakeDeleteAccountUseCase
import fr.skyle.escapy.fake.FakeWatchSessionUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.deleteaccount.ui.DeleteAccountViewModel
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
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "member", isGuest = false, email = "member@escapy.app"))

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
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "member", isGuest = false, email = "member@escapy.app"))

        // When
        viewModel.onDeleteClicked(currentPassword = "Wrong-2026")
        viewModel.onDeletionConfirmed(currentPassword = "Wrong-2026")

        // Then
        assertEquals(
            expected = AccountError.INVALID_CREDENTIALS,
            actual = viewModel.uiState.value.passwordError,
        )
    }
}
