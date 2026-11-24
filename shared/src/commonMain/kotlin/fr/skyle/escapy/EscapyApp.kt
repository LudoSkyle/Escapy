package fr.skyle.escapy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.ui.main.MainUiState
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
            MainUiState.Loading -> ESScreenStructure {}
            is MainUiState.Ready -> RootNavDisplay(isSignedIn = state.isSignedIn)
        }
    }
}
