package fr.skyle.escapy.ui.screen.deleteaccount.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.ui.core.error.accountErrorMessages
import fr.skyle.escapy.ui.core.error.message
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.deleteaccount.ui.DeleteAccountViewModel.DeleteAccountEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DeleteAccountRoute(
    navigateBack: () -> Unit,
    viewModel: DeleteAccountViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentPassword by rememberSaveable { mutableStateOf("") }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is DeleteAccountEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    DeleteAccountScreen(
        currentPassword = currentPassword,
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onCurrentPasswordChanged = { writtenPassword ->
            currentPassword = writtenPassword
            viewModel.onPasswordEdited()
        },
        onDeleteClicked = { viewModel.onDeleteClicked(currentPassword = currentPassword) },
        onDeletionConfirmed = { viewModel.onDeletionConfirmed(currentPassword = currentPassword) },
        onConfirmationDismissed = viewModel::onConfirmationDismissed,
    )
}
