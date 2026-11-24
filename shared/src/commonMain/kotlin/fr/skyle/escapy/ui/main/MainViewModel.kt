package fr.skyle.escapy.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The state of the whole app: whether a session is open decides the screens it can show. */
class MainViewModel(
    watchSessionUseCase: WatchSessionUseCase,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = watchSessionUseCase()
        .map<Session, MainUiState> { session -> MainUiState.Ready(isSignedIn = session is Session.SignedIn) }
        // A session that cannot be read is no session: the user signs in again
        .catch { exception ->
            Logger.e(throwable = exception) { "Failed to read the session" }
            emit(MainUiState.Ready(isSignedIn = false))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = MainUiState.Loading,
        )
}

sealed interface MainUiState {

    data object Loading : MainUiState

    data class Ready(val isSignedIn: Boolean) : MainUiState
}
