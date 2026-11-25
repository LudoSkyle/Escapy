package fr.skyle.escapy.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.domain.usecase.auth.SignOutUseCase
import fr.skyle.escapy.domain.usecase.profile.EnsureProfileUseCase
import fr.skyle.escapy.domain.usecase.profile.WatchProfileUseCase
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The state of the whole app. A session opens the screens of a signed-in user once its profile
 * exists: read from the cache of Firestore, or created by the server the first time. A profile
 * that disappears once read is never created again: its account is being deleted.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val watchSessionUseCase: WatchSessionUseCase,
    private val watchProfileUseCase: WatchProfileUseCase,
    private val ensureProfileUseCase: EnsureProfileUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUIState>(MainUIState.Loading)
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()

    // Incremented to read the profile again after a failure
    private val attempt = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            attempt
                .flatMapLatest { watchSessionUseCase() }
                .flatMapLatest { session ->
                    when (session) {
                        Session.SignedOut -> flowOf(MainUIState.SignedOut)
                        is Session.SignedIn -> profileState(userId = session.userId)
                    }
                }
                // A session that cannot be read is no session: the user signs in again
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the session" }

                    emit(MainUIState.SignedOut)
                }
                .collect { state ->
                    _uiState.update { state }
                }
        }
    }

    private fun profileState(userId: String): Flow<MainUIState> =
        channelFlow {
            send(MainUIState.Loading)
            var isEnsureRequested = false
            var isProfileRead = false
            try {
                watchProfileUseCase(userId = userId).collect { profile ->
                    when {
                        profile != null -> {
                            isProfileRead = true
                            send(MainUIState.SignedIn)
                        }
                        // Gone after it was read: the account is being deleted, the session ends next
                        isProfileRead -> Unit
                        // Missing the first time only: the server creates it, and the listener sees it
                        !isEnsureRequested -> {
                            isEnsureRequested = true
                            ensureProfileUseCase()
                        }
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to read or create the profile" }

                send(MainUIState.ProfileUnavailable)
            }
        }

    fun onRetryClicked() {
        attempt.update { value -> value + 1 }
    }

    fun onSignOutClicked() {
        viewModelScope.launch {
            try {
                signOutUseCase()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to sign out" }
            }
        }
    }

    sealed interface MainUIState {

        data object Loading : MainUIState

        data object SignedOut : MainUIState

        data object SignedIn : MainUIState

        /** Signed in, but the profile could not be read nor created: retry, or sign out. */
        data object ProfileUnavailable : MainUIState
    }
}
