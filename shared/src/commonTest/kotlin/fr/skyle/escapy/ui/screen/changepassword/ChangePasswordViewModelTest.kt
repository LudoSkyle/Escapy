package fr.skyle.escapy.ui.screen.changepassword

import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.fake.FakeChangePasswordUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.changepassword.ui.ChangePasswordViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChangePasswordViewModelTest : MainDispatcherTest() {

    private val changePasswordUseCase = FakeChangePasswordUseCase()

    private val viewModel = ChangePasswordViewModel(
        changePasswordUseCase = changePasswordUseCase,
    )

    @Test
    fun `a valid change is told done`() = runTest {
        // When
        viewModel.onChangeClicked(currentPassword = "Escapy-2026", newPassword = "Escapy-2027")

        // Then
        assertTrue(viewModel.uiState.value.isChanged)
    }

    @Test
    fun `a wrong current password is shown under it`() = runTest {
        // Given
        changePasswordUseCase.failure = InvalidCredentialsException()

        // When
        viewModel.onChangeClicked(currentPassword = "Wrong-2026", newPassword = "Escapy-2027")

        // Then
        assertEquals(
            expected = AccountError.INVALID_CREDENTIALS,
            actual = viewModel.uiState.value.currentPasswordError,
        )
    }
}
