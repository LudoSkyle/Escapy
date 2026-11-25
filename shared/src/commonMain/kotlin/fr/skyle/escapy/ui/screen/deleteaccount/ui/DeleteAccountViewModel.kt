package fr.skyle.escapy.ui.screen.deleteaccount.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.domain.model.Reauthentication
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.domain.usecase.account.DeleteAccountUseCase
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.core.error.toAccountError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Deletes the account, after a confirmation, and the current password for a member, or a new sign-in
 * with Google for a member who signs in with Google only: once deleted, the session ends and the app
 * opens the welcome screen.
 */
class DeleteAccountViewModel(
    private val watchSessionUseCase: WatchSessionUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeleteAccountUIState())
    val uiState: StateFlow<DeleteAccountUIState> = _uiState.asStateFlow()

    private val _event = Channel<DeleteAccountEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            watchSessionUseCase()
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the session" }
                }
                .collect { session ->
                    val signedIn = session as? Session.SignedIn ?: return@collect
                    _uiState.update { uiState ->
                        uiState.copy(
                            method = when {
                                signedIn.isGuest -> DeletionMethod.GUEST
                                signedIn.hasPassword -> DeletionMethod.PASSWORD
                                else -> DeletionMethod.GOOGLE
                            },
                        )
                    }
                }
        }
    }

    fun onPasswordEdited() {
        _uiState.update { uiState ->
            uiState.copy(passwordError = null)
        }
    }

    // The text being written stays in the screen, where each key is shown at once: given on deleting
    fun onDeleteClicked(currentPassword: String) {
        if (!canDelete(method = _uiState.value.method, currentPassword = currentPassword)) return
        _uiState.update { uiState ->
            uiState.copy(isConfirmationShown = true)
        }
    }

    fun onConfirmationDismissed() {
        if (_uiState.value.isDeleting) return
        _uiState.update { uiState ->
            uiState.copy(isConfirmationShown = false)
        }
    }

    // A guest or a member with a password: the Route asks Google for a token instead for the others
    fun onDeletionConfirmed(currentPassword: String) {
        delete(
            reauthentication = when (_uiState.value.method) {
                DeletionMethod.GUEST -> Reauthentication.None
                DeletionMethod.PASSWORD -> Reauthentication.WithPassword(password = currentPassword)
                DeletionMethod.GOOGLE -> return
            },
        )
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        delete(reauthentication = Reauthentication.WithGoogle(idToken = idToken))
    }

    fun onGoogleSignInFailed(exception: Exception) {
        Logger.e(throwable = exception) { "Google gave no token" }

        _uiState.update { uiState ->
            uiState.copy(isConfirmationShown = false)
        }
        viewModelScope.launch {
            _event.send(DeleteAccountEvent.ShowError(error = AccountError.UNKNOWN))
        }
    }

    private fun delete(reauthentication: Reauthentication) {
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isDeleting = true)
            }

            try {
                // The session changes: the app opens the welcome screen
                deleteAccountUseCase(reauthentication = reauthentication)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: InvalidCredentialsException) {
                _uiState.update { uiState ->
                    uiState.copy(passwordError = AccountError.INVALID_CREDENTIALS)
                }
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to delete the account" }

                _event.send(DeleteAccountEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(
                        isDeleting = false,
                        isConfirmationShown = false,
                    )
                }
            }
        }
    }

    enum class DeletionMethod {
        // Nothing to confirm with: the server does not ask a guest to sign in again
        GUEST,
        PASSWORD,
        GOOGLE,
    }

    data class DeleteAccountUIState(
        val method: DeletionMethod = DeletionMethod.PASSWORD,
        val passwordError: AccountError? = null,
        val isConfirmationShown: Boolean = false,
        val isDeleting: Boolean = false,
    )

    sealed interface DeleteAccountEvent {
        data class ShowError(val error: AccountError) : DeleteAccountEvent
    }

    companion object {
        // Only a member with a password has one to give
        fun canDelete(method: DeletionMethod, currentPassword: String): Boolean =
            method != DeletionMethod.PASSWORD || currentPassword.isNotEmpty()
    }
}
