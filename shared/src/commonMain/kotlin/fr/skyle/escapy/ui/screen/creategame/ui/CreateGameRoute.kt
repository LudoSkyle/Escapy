package fr.skyle.escapy.ui.screen.creategame.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.domain.validation.GameRules
import fr.skyle.escapy.ui.core.error.gameErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.creategame.ui.CreateGameViewModel.CreateGameEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateGameRoute(
    navigateBack: () -> Unit,
    navigateToGame: (gameId: String) -> Unit,
    viewModel: CreateGameViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var title by rememberSaveable { mutableStateOf("") }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = gameErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is CreateGameEvent.Created -> navigateToGame(event.gameId)
            is CreateGameEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    CreateGameScreen(
        title = title,
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onTitleChanged = { written ->
            // The server refuses a longer title: it is not worth typing
            title = written.take(GameRules.TITLE_MAX_LENGTH)
            viewModel.onTitleEdited()
        },
        onDurationDecreased = viewModel::onDurationDecreased,
        onDurationIncreased = viewModel::onDurationIncreased,
        onMaxPlayersDecreased = viewModel::onMaxPlayersDecreased,
        onMaxPlayersIncreased = viewModel::onMaxPlayersIncreased,
        onCreateClicked = { viewModel.onCreateClicked(title = title) },
    )
}
