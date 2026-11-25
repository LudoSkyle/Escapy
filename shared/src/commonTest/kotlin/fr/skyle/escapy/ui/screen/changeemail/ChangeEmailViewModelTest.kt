package fr.skyle.escapy.ui.screen.changeemail

import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.fake.FakeChangeEmailUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.changeemail.ui.ChangeEmailViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChangeEmailViewModelTest : MainDispatcherTest() {

    private val changeEmailUseCase = FakeChangeEmailUseCase()

    private val viewModel = ChangeEmailViewModel(
        changeEmailUseCase = changeEmailUseCase,
    )

    @Test
    fun `once sent the screen says to which email`() = runTest {
        // When
        viewModel.onChangeClicked(currentPassword = "Escapy-2026", newEmail = "new@escapy.app")

        // Then
        assertEquals(
            expected = "new@escapy.app",
            actual = viewModel.uiState.value.sentTo,
        )
    }

    @Test
    fun `a wrong current password is shown under it`() = runTest {
        // Given
        changeEmailUseCase.failure = InvalidCredentialsException()

        // When
        viewModel.onChangeClicked(currentPassword = "Wrong-2026", newEmail = "new@escapy.app")

        // Then
        assertEquals(
            expected = AccountError.INVALID_CREDENTIALS,
            actual = viewModel.uiState.value.passwordError,
        )
        assertNull(viewModel.uiState.value.sentTo)
    }
}
