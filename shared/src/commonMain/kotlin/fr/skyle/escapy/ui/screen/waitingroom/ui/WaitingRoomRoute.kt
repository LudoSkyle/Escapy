package fr.skyle.escapy.ui.screen.waitingroom.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.ui.core.error.gameErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.waitingroom.ui.WaitingRoomViewModel.WaitingRoomEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WaitingRoomRoute(
    gameId: String,
    navigateBack: () -> Unit,
    viewModel: WaitingRoomViewModel = koinViewModel(key = gameId) { parametersOf(gameId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = rememberESSnackbarState()
    val errorMessages = gameErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            WaitingRoomEvent.Closed -> navigateBack()
            is WaitingRoomEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    WaitingRoomScreen(
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onKickClicked = viewModel::onKickClicked,
        onKickDismissed = viewModel::onKickDismissed,
        onKickConfirmed = viewModel::onKickConfirmed,
        onLeaveClicked = viewModel::onLeaveClicked,
        onLeaveDismissed = viewModel::onLeaveDismissed,
        onLeaveConfirmed = viewModel::onLeaveConfirmed,
        onCancelClicked = viewModel::onCancelClicked,
        onCancelDismissed = viewModel::onCancelDismissed,
        onCancelConfirmed = viewModel::onCancelConfirmed,
    )
}
