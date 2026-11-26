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
    navigateToScanCode: () -> Unit,
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
            // Kept as typed: rewriting it while the keyboard composes would lose keys. The keyboard
            // types in capitals, and the code is cleaned before it is sent (GameRules.cleanJoinCode)
            joinCode = typed.take(JOIN_CODE_MAX_TYPED_LENGTH)
            viewModel.onJoinCodeEdited()
        },
        onJoinClicked = { viewModel.onJoinClicked(code = joinCode) },
        onScanClicked = navigateToScanCode,
        onCreateGameClicked = navigateToCreateGame,
        onGameClicked = navigateToGame,
    )
}

// Room for a space or two between the six characters, as the code is shown
private const val JOIN_CODE_MAX_TYPED_LENGTH = GameRules.JOIN_CODE_LENGTH + 2
