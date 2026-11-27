package fr.skyle.escapy.ui.screen.editmission.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.ui.core.error.gameErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.editmission.ui.EditMissionViewModel.EditMissionEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EditMissionRoute(
    gameId: String,
    missionId: String?,
    navigateBack: () -> Unit,
    viewModel: EditMissionViewModel = koinViewModel(key = "$gameId/$missionId") { parametersOf(gameId, missionId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isDeleteDialogShown by rememberSaveable { mutableStateOf(false) }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = gameErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            EditMissionEvent.Closed -> {
                // Closed first: a dialog is a window of its own, which would stay during the transition
                isDeleteDialogShown = false
                navigateBack()
            }
            EditMissionEvent.ActionFinished -> isDeleteDialogShown = false
            is EditMissionEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    EditMissionScreen(
        uiState = uiState,
        isDeleteDialogShown = isDeleteDialogShown,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onTitleChanged = viewModel::onTitleChanged,
        onStatementChanged = viewModel::onStatementChanged,
        onTypeChanged = viewModel::onTypeChanged,
        onAnswerChanged = viewModel::onAnswerChanged,
        onAnswerAdded = viewModel::onAnswerAdded,
        onAnswerRemoved = viewModel::onAnswerRemoved,
        onTriggerChanged = viewModel::onTriggerChanged,
        onFinalChanged = viewModel::onFinalChanged,
        onSaveClicked = viewModel::onSaveClicked,
        onDeleteClicked = { isDeleteDialogShown = true },
        onDeleteDismissed = { if (!uiState.isDeleting) isDeleteDialogShown = false },
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
    )
}
