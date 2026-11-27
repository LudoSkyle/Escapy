package fr.skyle.escapy.ui.screen.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.ui.core.error.gameErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.chat.ui.ChatViewModel.ChatEvent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatRoute(
    gameId: String,
    navigateBack: () -> Unit,
    viewModel: ChatViewModel = koinViewModel(key = gameId) { parametersOf(gameId) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var draft by rememberSaveable { mutableStateOf("") }
    val snackbarState = rememberESSnackbarState()
    val errorMessages = gameErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is ChatEvent.SendFailed -> {
                // The text comes back, unless the user started another message meanwhile
                if (draft.isEmpty()) draft = event.text
                snackbarState.show(message = errorMessages.getValue(event.error))
            }
        }
    }

    ChatScreen(
        uiState = uiState,
        draft = draft,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onDraftChanged = { text -> draft = text },
        onSendClicked = {
            viewModel.onSendClicked(text = draft)
            draft = ""
        },
    )
}
