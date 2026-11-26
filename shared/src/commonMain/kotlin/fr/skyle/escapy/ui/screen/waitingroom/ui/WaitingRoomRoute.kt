package fr.skyle.escapy.ui.screen.waitingroom.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.domain.model.GameState
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
    var playerIdToKick by rememberSaveable { mutableStateOf<String?>(null) }
    var isLeaveDialogShown by rememberSaveable { mutableStateOf(false) }
    var isCancelDialogShown by rememberSaveable { mutableStateOf(false) }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = gameErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            WaitingRoomEvent.Closed -> {
                // Closed first: a dialog is a window of its own, which would stay during the transition
                playerIdToKick = null
                isLeaveDialogShown = false
                isCancelDialogShown = false
                navigateBack()
            }
            WaitingRoomEvent.ActionFinished -> {
                playerIdToKick = null
                isLeaveDialogShown = false
                isCancelDialogShown = false
            }
            is WaitingRoomEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    // A player who left meanwhile is no longer to exclude: the dialog goes with them
    val playerToKick = (uiState.state as? GameState.Available)?.players?.firstOrNull { player ->
        player.userId == playerIdToKick
    }

    WaitingRoomScreen(
        uiState = uiState,
        playerToKick = playerToKick,
        isLeaveDialogShown = isLeaveDialogShown,
        isCancelDialogShown = isCancelDialogShown,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onKickClicked = { player -> playerIdToKick = player.userId },
        onKickDismissed = { if (!uiState.isKicking) playerIdToKick = null },
        onKickConfirmed = { playerToKick?.let { player -> viewModel.onKickConfirmed(playerId = player.userId) } },
        onLeaveClicked = { isLeaveDialogShown = true },
        onLeaveDismissed = { if (!uiState.isLeaving) isLeaveDialogShown = false },
        onLeaveConfirmed = viewModel::onLeaveConfirmed,
        onCancelClicked = { isCancelDialogShown = true },
        onCancelDismissed = { if (!uiState.isCancelling) isCancelDialogShown = false },
        onCancelConfirmed = viewModel::onCancelConfirmed,
    )
}
