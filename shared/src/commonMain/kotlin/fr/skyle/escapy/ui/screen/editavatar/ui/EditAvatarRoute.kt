package fr.skyle.escapy.ui.screen.editavatar.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.ui.core.error.accountErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.editavatar.ui.EditAvatarViewModel.EditAvatarEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditAvatarRoute(
    navigateBack: () -> Unit,
    viewModel: EditAvatarViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            EditAvatarEvent.Saved -> navigateBack()
            is EditAvatarEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    EditAvatarScreen(
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onAvatarSelected = viewModel::onAvatarSelected,
    )
}
