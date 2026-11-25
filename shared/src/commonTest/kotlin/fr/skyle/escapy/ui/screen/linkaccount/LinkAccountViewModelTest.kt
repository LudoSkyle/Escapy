package fr.skyle.escapy.ui.screen.linkaccount

import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.fake.FakeLinkGuestAccountUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.linkaccount.ui.LinkAccountViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LinkAccountViewModelTest : MainDispatcherTest() {

    private val linkGuestAccountUseCase = FakeLinkGuestAccountUseCase()

    private val viewModel = LinkAccountViewModel(
        linkGuestAccountUseCase = linkGuestAccountUseCase,
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
}
