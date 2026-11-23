package fr.skyle.escapy.ui.screens.lobby.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LobbyRoute(
    navigateBack: () -> Unit,
    lobbyViewModel: LobbyViewModel = hiltViewModel()
) {
    val lobbyState by lobbyViewModel.state.collectAsStateWithLifecycle()

    LobbyScreen(
        state = lobbyState,
        navigateBack = navigateBack,
    )
}