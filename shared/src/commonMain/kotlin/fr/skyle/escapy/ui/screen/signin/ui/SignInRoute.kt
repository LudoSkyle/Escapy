package fr.skyle.escapy.ui.screen.signin.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.ui.core.error.accountErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.signin.ui.SignInViewModel.SignInEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignInRoute(
    navigateBack: () -> Unit,
    navigateToForgotPassword: (email: String) -> Unit,
    viewModel: SignInViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is SignInEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    SignInScreen(
        email = email,
        password = password,
        isSigningIn = uiState.isSigningIn,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onEmailChanged = { writtenEmail -> email = writtenEmail },
        onPasswordChanged = { writtenPassword -> password = writtenPassword },
        onSignInClicked = { viewModel.onSignInClicked(email = email, password = password) },
        onForgotPasswordClicked = { navigateToForgotPassword(email) },
    )
}
