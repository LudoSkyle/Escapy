package fr.skyle.escapy.ui.screen.welcome

import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.fake.FakeSignInAsGuestUseCase
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

    private val viewModel = WelcomeViewModel(
        signInAsGuestUseCase = signInAsGuestUseCase,
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
}
