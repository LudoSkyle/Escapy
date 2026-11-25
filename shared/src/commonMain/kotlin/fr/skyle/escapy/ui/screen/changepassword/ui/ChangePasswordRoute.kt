package fr.skyle.escapy.ui.screen.changepassword.ui

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
import fr.skyle.escapy.ui.screen.changepassword.ui.ChangePasswordViewModel.ChangePasswordEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChangePasswordRoute(
    navigateBack: () -> Unit,
    viewModel: ChangePasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is ChangePasswordEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    ChangePasswordScreen(
        currentPassword = currentPassword,
        newPassword = newPassword,
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onCurrentPasswordChanged = { writtenPassword ->
            currentPassword = writtenPassword
            viewModel.onCurrentPasswordEdited()
        },
        onNewPasswordChanged = { writtenPassword -> newPassword = writtenPassword },
        onChangeClicked = { viewModel.onChangeClicked(currentPassword = currentPassword, newPassword = newPassword) },
    )
}
