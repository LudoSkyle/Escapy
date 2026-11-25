package fr.skyle.escapy.ui.screen.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.domain.usecase.auth.SignOutUseCase
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The home screen, for now a place for the games to come, and the way out of the account. */
class HomeViewModel(
    private val watchSessionUseCase: WatchSessionUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            watchSessionUseCase()
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the session" }
                }
                .collect { session ->
                    _uiState.update { uiState ->
                        uiState.copy(isGuest = (session as? Session.SignedIn)?.isGuest == true)
                    }
                }
        }
    }

    fun onSignOutClicked() {
        // A guest loses their account: they confirm first
        if (_uiState.value.isGuest) {
            _uiState.update { uiState ->
                uiState.copy(isGuestSignOutDialogShown = true)
            }
        } else {
            signOut()
        }
    }

    fun onGuestSignOutConfirmed() {
        _uiState.update { uiState ->
            uiState.copy(isGuestSignOutDialogShown = false)
        }
        signOut()
    }

    fun onGuestSignOutDismissed() {
        _uiState.update { uiState ->
            uiState.copy(isGuestSignOutDialogShown = false)
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            try {
                // The session changes: the app opens the welcome screen
                signOutUseCase()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to sign out" }
            }
        }
    }

    data class HomeUIState(
        val isGuest: Boolean = false,
        val isGuestSignOutDialogShown: Boolean = false,
    )
}
