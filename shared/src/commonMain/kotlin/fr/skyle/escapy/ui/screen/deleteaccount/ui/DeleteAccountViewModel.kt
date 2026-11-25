package fr.skyle.escapy.ui.screen.deleteaccount.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.exception.InvalidCredentialsException
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
 * Deletes the account, after a confirmation, and the current password for a member: once deleted,
 * the session ends and the app opens the welcome screen.
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
                    _uiState.update { uiState ->
                        uiState.copy(isGuest = (session as? Session.SignedIn)?.isGuest == true)
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
        if (!canDelete(isGuest = _uiState.value.isGuest, currentPassword = currentPassword)) return
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

    fun onDeletionConfirmed(currentPassword: String) {
        val state = _uiState.value
        if (state.isDeleting) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isDeleting = true)
            }

            try {
                // The session changes: the app opens the welcome screen
                deleteAccountUseCase(currentPassword = if (state.isGuest) null else currentPassword)
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

    data class DeleteAccountUIState(
        val isGuest: Boolean = false,
        val passwordError: AccountError? = null,
        val isConfirmationShown: Boolean = false,
        val isDeleting: Boolean = false,
    )

    sealed interface DeleteAccountEvent {
        data class ShowError(val error: AccountError) : DeleteAccountEvent
    }

    companion object {
        // A guest has no password to give
        fun canDelete(isGuest: Boolean, currentPassword: String): Boolean =
            isGuest || currentPassword.isNotEmpty()
    }
}
