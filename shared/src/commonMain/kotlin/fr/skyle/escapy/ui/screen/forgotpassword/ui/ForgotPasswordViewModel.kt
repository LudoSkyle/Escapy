package fr.skyle.escapy.ui.screen.forgotpassword.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.exception.InvalidEmailException
import fr.skyle.escapy.domain.usecase.auth.SendPasswordResetUseCase
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

/** Sends the email to reset a password. */
class ForgotPasswordViewModel(
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUIState())
    val uiState: StateFlow<ForgotPasswordUIState> = _uiState.asStateFlow()

    private val _event = Channel<ForgotPasswordEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    /** The email changed: its error no longer applies. */
    fun onEmailEdited() {
        _uiState.update { uiState ->
            uiState.copy(emailError = null)
        }
    }

    // The text being written stays in the screen, where each key is shown at once: given on sending
    fun onSendClicked(email: String) {
        if (email.isBlank() || _uiState.value.isSending) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isSending = true)
            }

            try {
                sendPasswordResetUseCase(email = email)

                _uiState.update { uiState ->
                    uiState.copy(sentTo = email.trim())
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: InvalidEmailException) {
                _uiState.update { uiState ->
                    uiState.copy(emailError = AccountError.INVALID_EMAIL)
                }
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to send the email to reset a password" }

                _event.send(ForgotPasswordEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isSending = false)
                }
            }
        }
    }

    data class ForgotPasswordUIState(
        val emailError: AccountError? = null,
        val isSending: Boolean = false,
        // The address the email was sent to, once sent: the screen says so instead of its form
        val sentTo: String? = null,
    )

    sealed interface ForgotPasswordEvent {
        data class ShowError(val error: AccountError) : ForgotPasswordEvent
    }
}
