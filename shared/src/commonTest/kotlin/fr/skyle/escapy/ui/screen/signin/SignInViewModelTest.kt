package fr.skyle.escapy.ui.screen.signin

import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.fake.FakeSignInWithEmailUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.signin.ui.SignInViewModel
import fr.skyle.escapy.ui.screen.signin.ui.SignInViewModel.SignInEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SignInViewModelTest : MainDispatcherTest() {

    private val signInWithEmailUseCase = FakeSignInWithEmailUseCase()

    private val viewModel = SignInViewModel(
        signInWithEmailUseCase = signInWithEmailUseCase,
    )

    @Test
    fun `nothing is sent without an email and a password`() = runTest {
        // When
        viewModel.onSignInClicked(email = "player@escapy.app", password = "")

        // Then
        assertFalse(SignInViewModel.canSignIn(email = "player@escapy.app", password = ""))
        assertEquals(
            expected = emptyList(),
            actual = signInWithEmailUseCase.calls,
        )
    }

    @Test
    fun `a second touch while signing in sends nothing more`() = runTest {
        // Given
        signInWithEmailUseCase.gate = CompletableDeferred()

        // When
        viewModel.onSignInClicked(email = "player@escapy.app", password = "Escapy-2026")
        viewModel.onSignInClicked(email = "player@escapy.app", password = "Escapy-2026")

        // Then
        assertTrue(viewModel.uiState.value.isSigningIn)
        assertEquals(
            expected = listOf("player@escapy.app"),
            actual = signInWithEmailUseCase.calls,
        )
    }

    @Test
    fun `wrong credentials are told and the form is usable again`() = runTest {
        // Given
        signInWithEmailUseCase.failure = InvalidCredentialsException()

        // When
        viewModel.onSignInClicked(email = "player@escapy.app", password = "Wrong-2026")

        // Then
        assertEquals(
            expected = SignInEvent.ShowError(error = AccountError.INVALID_CREDENTIALS),
            actual = viewModel.event.first(),
        )
        assertFalse(viewModel.uiState.value.isSigningIn)
    }
}
