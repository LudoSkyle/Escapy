package fr.skyle.escapy.ui.screen.play.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.ui.core.error.gameErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.play.ui.PlayViewModel.PlayEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** The minutes the add-time dialog offers first. */
private const val DEFAULT_MINUTES_TO_ADD = 5

@Composable
fun PlayRoute(
    gameId: String,
    navigateBack: () -> Unit,
    navigateToChat: () -> Unit,
    viewModel: PlayViewModel = koinViewModel(key = gameId) { parametersOf(gameId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var playerIdToKick by rememberSaveable { mutableStateOf<String?>(null) }
    var minutesToAdd by rememberSaveable { mutableStateOf<Int?>(null) }
    var isEndDialogShown by rememberSaveable { mutableStateOf(false) }
    var isLeaveDialogShown by rememberSaveable { mutableStateOf(false) }
    var isDeleteDialogShown by rememberSaveable { mutableStateOf(false) }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = gameErrorMessages()

    // Back from the background, the clock of the device may have moved: the offset is estimated again
    LifecycleResumeEffect(viewModel) {
        viewModel.onResumed()
        onPauseOrDispose {}
    }

    fun closeDialogs() {
        playerIdToKick = null
        minutesToAdd = null
        isEndDialogShown = false
        isLeaveDialogShown = false
        isDeleteDialogShown = false
    }

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            PlayEvent.Closed -> {
                // Closed first: a dialog is a window of its own, which would stay during the transition
                closeDialogs()
                navigateBack()
            }
            PlayEvent.ActionFinished -> closeDialogs()
            is PlayEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    val isActing = uiState.action != null
    // A player who left meanwhile is no longer to exclude: the dialog goes with them
    val playerToKick = (uiState.state as? GameState.Available)?.players?.firstOrNull { player ->
        player.userId == playerIdToKick
    }

    PlayScreen(
        uiState = uiState,
        playerToKick = playerToKick,
        minutesToAdd = minutesToAdd,
        isEndDialogShown = isEndDialogShown,
        isLeaveDialogShown = isLeaveDialogShown,
        isDeleteDialogShown = isDeleteDialogShown,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onChatClicked = navigateToChat,
        onPauseClicked = viewModel::onPauseClicked,
        onResumeClicked = viewModel::onResumeClicked,
        onAddTimeClicked = { minutesToAdd = DEFAULT_MINUTES_TO_ADD },
        onMinutesToAddChanged = { minutes -> minutesToAdd = minutes },
        onAddTimeDismissed = { if (!isActing) minutesToAdd = null },
        onAddTimeConfirmed = { minutesToAdd?.let(viewModel::onAddTimeConfirmed) },
        onEndClicked = { isEndDialogShown = true },
        onEndDismissed = { if (!isActing) isEndDialogShown = false },
        onEndConfirmed = viewModel::onEndConfirmed,
        onKickClicked = { player -> playerIdToKick = player.userId },
        onKickDismissed = { if (!isActing) playerIdToKick = null },
        onKickConfirmed = { playerToKick?.let { player -> viewModel.onKickConfirmed(playerId = player.userId) } },
        onLeaveClicked = { isLeaveDialogShown = true },
        onLeaveDismissed = { if (!isActing) isLeaveDialogShown = false },
        onLeaveConfirmed = viewModel::onLeaveConfirmed,
        onDeleteClicked = { isDeleteDialogShown = true },
        onDeleteDismissed = { if (!isActing) isDeleteDialogShown = false },
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
    )
}
