package fr.skyle.escapy.ui.screen.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onSignOutClicked = viewModel::onSignOutClicked,
        onGuestSignOutConfirmed = viewModel::onGuestSignOutConfirmed,
        onGuestSignOutDismissed = viewModel::onGuestSignOutDismissed,
    )
}
