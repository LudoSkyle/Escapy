package fr.skyle.escapy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.catalog.ESCatalog
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.ui.main.MainViewModel.MainUIState
import fr.skyle.escapy.ui.main.ProfileUnavailableScreen
import fr.skyle.escapy.ui.main.MainViewModel
import fr.skyle.escapy.ui.main.navigation.RootNavDisplay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EscapyApp() {
    val viewModel = koinViewModel<MainViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ESTheme {
        when (uiState) {
            // The session and the profile come from the local caches of Firebase: a blank frame, briefly,
            // longer only the first time a profile is created
            MainUIState.Loading -> ESScreenStructure(style = ESScreenStructureDefaults.Style.STANDARD) {}
            MainUIState.SignedOut -> RootNavDisplay(isSignedIn = false)
            MainUIState.SignedIn -> RootNavDisplay(isSignedIn = true)
            MainUIState.ProfileUnavailable -> ProfileUnavailableScreen(
                onRetryClicked = viewModel::onRetryClicked,
                onSignOutClicked = viewModel::onSignOutClicked,
            )
        }
    }
}

/**
 * The catalog of the design system, instead of the app: opened by a debug build only, on demand
 * (see docs/UI/03_DESIGN_SYSTEM.md).
 */
@Composable
fun EscapyDesignSystemCatalog() {
    ESTheme {
        ESCatalog()
    }
}
