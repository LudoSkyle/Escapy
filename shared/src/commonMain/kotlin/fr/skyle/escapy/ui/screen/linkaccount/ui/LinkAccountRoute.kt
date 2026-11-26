package fr.skyle.escapy.ui.screen.linkaccount.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.platform.google.rememberGoogleSignInLauncher
import fr.skyle.escapy.platform.google.requestTokensOrNull
import fr.skyle.escapy.ui.core.error.accountErrorMessages
import fr.skyle.escapy.ui.core.error.message
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.linkaccount.ui.LinkAccountViewModel.LinkAccountEvent
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LinkAccountRoute(
    navigateBack: () -> Unit,
    viewModel: LinkAccountViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()
    val googleSignIn = rememberGoogleSignInLauncher()
    val scope = rememberCoroutineScope()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is LinkAccountEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    LinkAccountScreen(
        email = email,
        password = password,
        uiState = uiState,
        isGoogleAvailable = googleSignIn.isAvailable,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onEmailChanged = { writtenEmail ->
            email = writtenEmail
            viewModel.onEmailEdited()
        },
        onPasswordChanged = { writtenPassword -> password = writtenPassword },
        onLinkClicked = { viewModel.onLinkClicked(email = email, password = password) },
        onLinkWithGoogleClicked = {
            scope.launch {
                googleSignIn.requestTokensOrNull(onFailure = viewModel::onGoogleSignInFailed)
                    ?.let(viewModel::onGoogleTokensReceived)
            }
        },
    )
}
