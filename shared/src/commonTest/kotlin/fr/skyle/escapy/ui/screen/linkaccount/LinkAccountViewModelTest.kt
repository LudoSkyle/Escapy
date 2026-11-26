package fr.skyle.escapy.ui.screen.linkaccount

import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.GoogleAccountAlreadyInUseException
import fr.skyle.escapy.domain.model.GoogleTokens
import fr.skyle.escapy.fake.FakeLinkGuestAccountUseCase
import fr.skyle.escapy.fake.FakeLinkGuestWithGoogleUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.linkaccount.ui.LinkAccountViewModel
import fr.skyle.escapy.ui.screen.linkaccount.ui.LinkAccountViewModel.LinkAccountEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LinkAccountViewModelTest : MainDispatcherTest() {

    private val linkGuestAccountUseCase = FakeLinkGuestAccountUseCase()
    private val linkGuestWithGoogleUseCase = FakeLinkGuestWithGoogleUseCase()

    private val viewModel = LinkAccountViewModel(
        linkGuestAccountUseCase = linkGuestAccountUseCase,
        linkGuestWithGoogleUseCase = linkGuestWithGoogleUseCase,
    )

    @Test
    fun `a weak password sends nothing`() = runTest {
        // When
        viewModel.onLinkClicked(email = "guest@escapy.app", password = "escapy")

        // Then
        assertEquals(
            expected = emptyList(),
            actual = linkGuestAccountUseCase.calls,
        )
    }

    @Test
    fun `once linked the screen says to which email`() = runTest {
        // When
        viewModel.onLinkClicked(email = " guest@escapy.app ", password = "Escapy-2026")

        // Then
        assertEquals(
            expected = "guest@escapy.app",
            actual = viewModel.uiState.value.linkedTo,
        )
    }

    @Test
    fun `an email used by another account is shown under the email`() = runTest {
        // Given
        linkGuestAccountUseCase.failure = EmailAlreadyInUseException()

        // When
        viewModel.onLinkClicked(email = "guest@escapy.app", password = "Escapy-2026")

        // Then
        assertEquals(
            expected = AccountError.EMAIL_ALREADY_IN_USE,
            actual = viewModel.uiState.value.emailError,
        )
    }

    @Test
    fun `once linked to Google the screen says so`() = runTest {
        // When
        viewModel.onGoogleTokensReceived(tokens = GoogleTokens(idToken = "token", accessToken = null))

        // Then
        assertEquals(
            expected = listOf("token"),
            actual = linkGuestWithGoogleUseCase.calls,
        )
        assertTrue(viewModel.uiState.value.isLinkedWithGoogle)
    }

    @Test
    fun `a Google account that already has an account is told`() = runTest {
        // Given
        linkGuestWithGoogleUseCase.failure = GoogleAccountAlreadyInUseException()

        // When
        viewModel.onGoogleTokensReceived(tokens = GoogleTokens(idToken = "token", accessToken = null))

        // Then
        assertEquals(
            expected = LinkAccountEvent.ShowError(error = AccountError.GOOGLE_ACCOUNT_ALREADY_IN_USE),
            actual = viewModel.event.first(),
        )
        assertFalse(viewModel.uiState.value.isLinkedWithGoogle)
    }
}
