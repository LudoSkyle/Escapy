package fr.skyle.escapy.ui.screen.editusername.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.ui.core.error.accountErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.editusername.ui.EditUsernameViewModel.EditUsernameEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditUsernameRoute(
    currentUsername: String,
    navigateBack: () -> Unit,
    viewModel: EditUsernameViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf(currentUsername) }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            EditUsernameEvent.Saved -> navigateBack()
            is EditUsernameEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    EditUsernameScreen(
        username = username,
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onUsernameChanged = { writtenUsername ->
            username = writtenUsername
            viewModel.onUsernameEdited()
        },
        onSaveClicked = { viewModel.onSaveClicked(username = username) },
    )
}
