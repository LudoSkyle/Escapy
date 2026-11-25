package fr.skyle.escapy.ui.screen.welcome.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.platform.google.rememberGoogleSignInLauncher
import fr.skyle.escapy.platform.google.requestIdTokenOrNull
import fr.skyle.escapy.ui.core.error.accountErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.welcome.ui.WelcomeViewModel.WelcomeEvent
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeRoute(
    navigateToSignIn: () -> Unit,
    navigateToSignUp: () -> Unit,
    viewModel: WelcomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()
    val googleSignIn = rememberGoogleSignInLauncher()
    val scope = rememberCoroutineScope()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is WelcomeEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    WelcomeScreen(
        isSigningInAsGuest = uiState.isSigningInAsGuest,
        isSigningInWithGoogle = uiState.isSigningInWithGoogle,
        isGoogleAvailable = googleSignIn.isAvailable,
        snackbarState = snackbarState,
        onSignInClicked = navigateToSignIn,
        onSignUpClicked = navigateToSignUp,
        onContinueWithGoogleClicked = {
            scope.launch {
                googleSignIn.requestIdTokenOrNull(onFailure = viewModel::onGoogleSignInFailed)
                    ?.let(viewModel::onGoogleIdTokenReceived)
            }
        },
        onContinueAsGuestClicked = viewModel::onContinueAsGuestClicked,
    )
}
