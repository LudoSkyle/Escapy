package fr.skyle.escapy.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The state of the whole app: whether a session is open decides the screens it can show. */
class MainViewModel(
    private val watchSessionUseCase: WatchSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUIState>(MainUIState.Loading)
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            watchSessionUseCase()
                // A session that cannot be read is no session: the user signs in again
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the session" }

                    _uiState.update {
                        MainUIState.Ready(isSignedIn = false)
                    }
                }
                .collect { session ->
                    _uiState.update {
                        MainUIState.Ready(isSignedIn = session is Session.SignedIn)
                    }
                }
        }
    }

    sealed interface MainUIState {

        data object Loading : MainUIState

        data class Ready(val isSignedIn: Boolean) : MainUIState
    }
}
