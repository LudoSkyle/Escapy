package fr.skyle.escapy.ui.screen.changeemail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.domain.exception.InvalidEmailException
import fr.skyle.escapy.domain.usecase.account.ChangeEmailUseCase
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.core.error.toAccountError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Sends a link to a new email, after the current password: the address changes once it is opened. */
class ChangeEmailViewModel(
    private val changeEmailUseCase: ChangeEmailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangeEmailUIState())
    val uiState: StateFlow<ChangeEmailUIState> = _uiState.asStateFlow()

    private val _event = Channel<ChangeEmailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun onPasswordEdited() {
        _uiState.update { uiState ->
            uiState.copy(passwordError = null)
        }
    }

    fun onEmailEdited() {
        _uiState.update { uiState ->
            uiState.copy(emailError = null)
        }
    }

    // The text being written stays in the screen, where each key is shown at once: given on sending
    fun onChangeClicked(currentPassword: String, newEmail: String) {
        if (currentPassword.isEmpty() || newEmail.isBlank() || _uiState.value.isChanging) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isChanging = true)
            }

            try {
                changeEmailUseCase(currentPassword = currentPassword, newEmail = newEmail)

                _uiState.update { uiState ->
                    uiState.copy(sentTo = newEmail.trim())
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: InvalidCredentialsException) {
                _uiState.update { uiState ->
                    uiState.copy(passwordError = AccountError.INVALID_CREDENTIALS)
                }
            } catch (exception: EmailAlreadyInUseException) {
                _uiState.update { uiState ->
                    uiState.copy(emailError = AccountError.EMAIL_ALREADY_IN_USE)
                }
            } catch (exception: InvalidEmailException) {
                _uiState.update { uiState ->
                    uiState.copy(emailError = AccountError.INVALID_EMAIL)
                }
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to change the email" }

                _event.send(ChangeEmailEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isChanging = false)
                }
            }
        }
    }

    data class ChangeEmailUIState(
        val passwordError: AccountError? = null,
        val emailError: AccountError? = null,
        val isChanging: Boolean = false,
        // The new email the link was sent to, once sent
        val sentTo: String? = null,
    )

    sealed interface ChangeEmailEvent {
        data class ShowError(val error: AccountError) : ChangeEmailEvent
    }
}
