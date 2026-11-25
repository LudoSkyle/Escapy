package fr.skyle.escapy.ui.screen.linkaccount.ui

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
import fr.skyle.escapy.ui.screen.linkaccount.ui.LinkAccountViewModel.LinkAccountEvent
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

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is LinkAccountEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    LinkAccountScreen(
        email = email,
        password = password,
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onEmailChanged = { writtenEmail ->
            email = writtenEmail
            viewModel.onEmailEdited()
        },
        onPasswordChanged = { writtenPassword -> password = writtenPassword },
        onLinkClicked = { viewModel.onLinkClicked(email = email, password = password) },
    )
}
