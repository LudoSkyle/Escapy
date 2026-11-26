package fr.skyle.escapy.ui.screen.welcome

import fr.skyle.escapy.domain.exception.AccountExistsWithPasswordException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.model.GoogleTokens
import fr.skyle.escapy.fake.FakeSignInAsGuestUseCase
import fr.skyle.escapy.fake.FakeSignInWithGoogleUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.welcome.ui.WelcomeViewModel
import fr.skyle.escapy.ui.screen.welcome.ui.WelcomeViewModel.WelcomeEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WelcomeViewModelTest : MainDispatcherTest() {

    private val signInAsGuestUseCase = FakeSignInAsGuestUseCase()
    private val signInWithGoogleUseCase = FakeSignInWithGoogleUseCase()

    private val viewModel = WelcomeViewModel(
        signInAsGuestUseCase = signInAsGuestUseCase,
        signInWithGoogleUseCase = signInWithGoogleUseCase,
    )

    @Test
    fun `a guest is signed in once however many touches`() = runTest {
        // Given
        signInAsGuestUseCase.gate = CompletableDeferred()

        // When
        viewModel.onContinueAsGuestClicked()
        viewModel.onContinueAsGuestClicked()

        // Then
        assertEquals(
            expected = listOf("guest"),
            actual = signInAsGuestUseCase.calls,
        )
    }

    @Test
    fun `a failure is told by a message`() = runTest {
        // Given
        signInAsGuestUseCase.failure = NetworkUnavailableException()

        // When
        viewModel.onContinueAsGuestClicked()

        // Then
        assertEquals(
            expected = WelcomeEvent.ShowError(error = AccountError.NETWORK),
            actual = viewModel.event.first(),
        )
    }

    @Test
    fun `the token of Google signs the user in`() = runTest {
        // When
        viewModel.onGoogleTokensReceived(tokens = GoogleTokens(idToken = "token", accessToken = null))

        // Then
        assertEquals(
            expected = listOf("token"),
            actual = signInWithGoogleUseCase.calls,
        )
    }

    @Test
    fun `no guest is signed in while Google signs the user in`() = runTest {
        // Given
        signInWithGoogleUseCase.gate = CompletableDeferred()

        // When
        viewModel.onGoogleTokensReceived(tokens = GoogleTokens(idToken = "token", accessToken = null))
        viewModel.onContinueAsGuestClicked()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = signInAsGuestUseCase.calls,
        )
    }

    @Test
    fun `an email that already has a password is told`() = runTest {
        // Given
        signInWithGoogleUseCase.failure = AccountExistsWithPasswordException()

        // When
        viewModel.onGoogleTokensReceived(tokens = GoogleTokens(idToken = "token", accessToken = null))

        // Then
        assertEquals(
            expected = WelcomeEvent.ShowError(error = AccountError.ACCOUNT_EXISTS_WITH_PASSWORD),
            actual = viewModel.event.first(),
        )
    }

    @Test
    fun `a Google sign-in that failed is told by a message`() = runTest {
        // When
        viewModel.onGoogleSignInFailed(exception = IllegalStateException("No token"))

        // Then
        assertEquals(
            expected = WelcomeEvent.ShowError(error = AccountError.UNKNOWN),
            actual = viewModel.event.first(),
        )
    }
}
