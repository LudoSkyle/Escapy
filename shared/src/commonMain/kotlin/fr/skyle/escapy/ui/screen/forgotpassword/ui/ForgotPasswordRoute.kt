package fr.skyle.escapy.ui.screen.forgotpassword.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.ui.core.error.accountErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.forgotpassword.ui.ForgotPasswordViewModel.ForgotPasswordEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordRoute(
    email: String,
    navigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // The email written on the sign-in screen, if any, ready to send
    var writtenEmail by rememberSaveable { mutableStateOf(email) }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is ForgotPasswordEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    ForgotPasswordScreen(
        email = writtenEmail,
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onEmailChanged = { changedEmail ->
            writtenEmail = changedEmail
            viewModel.onEmailEdited()
        },
        onSendClicked = { viewModel.onSendClicked(email = writtenEmail) },
    )
}
