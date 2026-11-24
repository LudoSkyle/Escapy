package fr.skyle.escapy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.catalog.ESCatalog
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.ui.main.MainViewModel.MainUIState
import fr.skyle.escapy.ui.main.MainViewModel
import fr.skyle.escapy.ui.main.navigation.RootNavDisplay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EscapyApp() {
    val viewModel = koinViewModel<MainViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ESTheme {
        when (val state = uiState) {
            // The first session comes from the local cache of Firebase: a blank frame, briefly
            MainUIState.Loading -> ESScreenStructure(style = ESScreenStructureDefaults.Style.STANDARD) {}
            is MainUIState.Ready -> RootNavDisplay(isSignedIn = state.isSignedIn)
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
