package fr.skyle.escapy.ui.screen.forgotpassword

import fr.skyle.escapy.domain.exception.InvalidEmailException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.fake.FakeSendPasswordResetUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.forgotpassword.ui.ForgotPasswordViewModel
import fr.skyle.escapy.ui.screen.forgotpassword.ui.ForgotPasswordViewModel.ForgotPasswordEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ForgotPasswordViewModelTest : MainDispatcherTest() {

    private val sendPasswordResetUseCase = FakeSendPasswordResetUseCase()

    private val viewModel = ForgotPasswordViewModel(
        sendPasswordResetUseCase = sendPasswordResetUseCase,
    )

    @Test
    fun `once sent the screen says to which address`() = runTest {
        // When
        viewModel.onSendClicked(email = "  player@escapy.app ")

        // Then
        assertEquals(
            expected = "player@escapy.app",
            actual = viewModel.uiState.value.sentTo,
        )
    }

    @Test
    fun `an invalid email is shown under the email`() = runTest {
        // Given
        sendPasswordResetUseCase.failure = InvalidEmailException()

        // When
        viewModel.onSendClicked(email = "player@escapy")

        // Then
        assertEquals(
            expected = AccountError.INVALID_EMAIL,
            actual = viewModel.uiState.value.emailError,
        )
        assertNull(viewModel.uiState.value.sentTo)
    }

    @Test
    fun `a network failure is told by a message`() = runTest {
        // Given
        sendPasswordResetUseCase.failure = NetworkUnavailableException()

        // When
        viewModel.onSendClicked(email = "player@escapy.app")

        // Then
        assertEquals(
            expected = ForgotPasswordEvent.ShowError(error = AccountError.NETWORK),
            actual = viewModel.event.first(),
        )
    }
}
