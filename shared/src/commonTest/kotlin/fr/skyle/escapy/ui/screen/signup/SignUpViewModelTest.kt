package fr.skyle.escapy.ui.screen.signup

import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.fake.FakeSignUpWithEmailUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.signup.ui.SignUpViewModel
import fr.skyle.escapy.ui.screen.signup.ui.SignUpViewModel.SignUpEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class SignUpViewModelTest : MainDispatcherTest() {

    private val signUpWithEmailUseCase = FakeSignUpWithEmailUseCase()

    private val viewModel = SignUpViewModel(
        signUpWithEmailUseCase = signUpWithEmailUseCase,
    )

    @Test
    fun `a password that breaks the policy sends nothing`() = runTest {
        // When
        viewModel.onSignUpClicked(email = "player@escapy.app", password = "Escapy")

        // Then
        assertFalse(SignUpViewModel.canSignUp(email = "player@escapy.app", password = "Escapy"))
        assertEquals(
            expected = emptyList(),
            actual = signUpWithEmailUseCase.calls,
        )
    }

    @Test
    fun `a valid form creates the account`() = runTest {
        // Given
        // When
        viewModel.onSignUpClicked(email = "player@escapy.app", password = "Escapy-2026")

        // Then
        assertEquals(
            expected = listOf("player@escapy.app"),
            actual = signUpWithEmailUseCase.calls,
        )
    }

    @Test
    fun `an email already used is shown under the email until it changes`() = runTest {
        // Given
        signUpWithEmailUseCase.failure = EmailAlreadyInUseException()

        // When
        viewModel.onSignUpClicked(email = "player@escapy.app", password = "Escapy-2026")
        val error = viewModel.uiState.value.emailError
        viewModel.onEmailEdited()

        // Then
        assertEquals(
            expected = AccountError.EMAIL_ALREADY_IN_USE,
            actual = error,
        )
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `a network failure is told by a message`() = runTest {
        // Given
        signUpWithEmailUseCase.failure = NetworkUnavailableException()

        // When
        viewModel.onSignUpClicked(email = "player@escapy.app", password = "Escapy-2026")

        // Then
        assertEquals(
            expected = SignUpEvent.ShowError(error = AccountError.NETWORK),
            actual = viewModel.event.first(),
        )
    }
}
