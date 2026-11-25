package fr.skyle.escapy.ui.screen.changeemail.ui

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
import fr.skyle.escapy.ui.screen.changeemail.ui.ChangeEmailViewModel.ChangeEmailEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChangeEmailRoute(
    navigateBack: () -> Unit,
    viewModel: ChangeEmailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newEmail by rememberSaveable { mutableStateOf("") }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is ChangeEmailEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    ChangeEmailScreen(
        currentPassword = currentPassword,
        newEmail = newEmail,
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onCurrentPasswordChanged = { writtenPassword ->
            currentPassword = writtenPassword
            viewModel.onPasswordEdited()
        },
        onNewEmailChanged = { writtenEmail ->
            newEmail = writtenEmail
            viewModel.onEmailEdited()
        },
        onChangeClicked = { viewModel.onChangeClicked(currentPassword = currentPassword, newEmail = newEmail) },
    )
}
