package fr.skyle.escapy.ui.screen.editusername

import fr.skyle.escapy.domain.exception.InvalidUsernameException
import fr.skyle.escapy.fake.FakeUpdateUsernameUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.screen.editusername.ui.EditUsernameViewModel
import fr.skyle.escapy.ui.screen.editusername.ui.EditUsernameViewModel.EditUsernameEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EditUsernameViewModelTest : MainDispatcherTest() {

    private val updateUsernameUseCase = FakeUpdateUsernameUseCase()

    private val viewModel = EditUsernameViewModel(
        updateUsernameUseCase = updateUsernameUseCase,
    )

    @Test
    fun `a saved username closes the screen`() = runTest {
        // When
        viewModel.onSaveClicked(username = "Énigme Master")

        // Then
        assertEquals(
            expected = EditUsernameEvent.Saved,
            actual = viewModel.event.first(),
        )
    }

    @Test
    fun `a username refused by the server is shown under the field until it changes`() = runTest {
        // Given
        updateUsernameUseCase.failure = InvalidUsernameException()

        // When
        viewModel.onSaveClicked(username = "<b>")
        val error = viewModel.uiState.value.usernameError
        viewModel.onUsernameEdited()

        // Then
        assertEquals(
            expected = AccountError.INVALID_USERNAME,
            actual = error,
        )
        assertNull(viewModel.uiState.value.usernameError)
    }
}
