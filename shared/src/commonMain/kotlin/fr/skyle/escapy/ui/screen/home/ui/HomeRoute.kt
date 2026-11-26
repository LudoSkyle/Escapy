package fr.skyle.escapy.ui.screen.home.ui

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
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel.HomeEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    navigateToProfile: () -> Unit,
    navigateToCreateGame: () -> Unit,
    navigateToGame: (gameId: String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var joinCode by rememberSaveable { mutableStateOf("") }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = gameErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is HomeEvent.OpenGame -> {
                joinCode = ""
                navigateToGame(event.gameId)
            }
            is HomeEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    HomeScreen(
        joinCode = joinCode,
        uiState = uiState,
        snackbarState = snackbarState,
        onProfileClicked = navigateToProfile,
        onJoinCodeChanged = { typed ->
            // A code is letters and digits, read in capitals: nothing else is worth typing
            joinCode = typed.filter { character -> character.isLetterOrDigit() }
                .uppercase()
                .take(GameRules.JOIN_CODE_LENGTH)
            viewModel.onJoinCodeEdited()
        },
        onJoinClicked = { viewModel.onJoinClicked(code = joinCode) },
        onCreateGameClicked = navigateToCreateGame,
        onGameClicked = navigateToGame,
    )
}
