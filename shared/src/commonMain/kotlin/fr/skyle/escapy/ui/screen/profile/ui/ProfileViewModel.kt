package fr.skyle.escapy.ui.screen.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.Profile
import fr.skyle.escapy.domain.model.Reauthentication
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.domain.usecase.account.DeleteAccountUseCase
import fr.skyle.escapy.domain.usecase.auth.SignOutUseCase
import fr.skyle.escapy.domain.usecase.profile.WatchProfileUseCase
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.core.error.toAccountError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The profile of the user: their name, their avatar, their account, and the way out of it. */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val watchSessionUseCase: WatchSessionUseCase,
    private val watchProfileUseCase: WatchProfileUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

    private val _event = Channel<ProfileEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            watchSessionUseCase()
                .flatMapLatest { session ->
                    if (session is Session.SignedIn) {
                        _uiState.update { uiState ->
                            uiState.copy(
                                isGuest = session.isGuest,
                                email = session.email,
                                hasPassword = session.hasPassword,
                            )
                        }
                        watchProfileUseCase(userId = session.userId)
                    } else {
                        emptyFlow()
                    }
                }
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the profile" }
                }
                .collect { profile ->
                    _uiState.update { uiState ->
                        uiState.copy(profile = profile)
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

    fun onGuestSignOutDismissed() {
        if (_uiState.value.isSigningOut) return
        _uiState.update { uiState ->
            uiState.copy(isGuestSignOutDialogShown = false)
        }
    }

    fun onGuestSignOutConfirmed() {
        signOut()
    }

    private fun signOut() {
        if (_uiState.value.isSigningOut) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isSigningOut = true)
            }

            try {
                // A guest account nobody can sign in to again is deleted, not left on the server.
                // The session changes: the app opens the welcome screen
                if (_uiState.value.isGuest) {
                    deleteAccountUseCase(reauthentication = Reauthentication.None)
                } else {
                    signOutUseCase()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to sign out" }

                _event.send(ProfileEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(
                        isSigningOut = false,
                        isGuestSignOutDialogShown = false,
                    )
                }
            }
        }
    }

    data class ProfileUIState(
        val profile: Profile? = null,
        val isGuest: Boolean = false,
        val email: String? = null,
        val hasPassword: Boolean = false,
        val isGuestSignOutDialogShown: Boolean = false,
        val isSigningOut: Boolean = false,
    ) {
        // A member with no password signs in with Google only
        val isSignedInWithGoogleOnly: Boolean
            get() = !isGuest && !hasPassword
    }

    sealed interface ProfileEvent {
        data class ShowError(val error: AccountError) : ProfileEvent
    }
}
