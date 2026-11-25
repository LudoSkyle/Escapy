package fr.skyle.escapy.ui.screen.signup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.InvalidEmailException
import fr.skyle.escapy.domain.usecase.auth.SignUpWithEmailUseCase
import fr.skyle.escapy.domain.validation.PasswordPolicy
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

class SignUpViewModel(
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUIState())
    val uiState: StateFlow<SignUpUIState> = _uiState.asStateFlow()

    private val _event = Channel<SignUpEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    /** The email changed: its error no longer applies. */
    fun onEmailEdited() {
        _uiState.update { uiState ->
            uiState.copy(emailError = null)
        }
    }

    // The text being written stays in the screen, where each key is shown at once: given on sending
    fun onSignUpClicked(email: String, password: String) {
        if (!canSignUp(email = email, password = password) || _uiState.value.isSigningUp) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isSigningUp = true)
            }

            try {
                // The session changes: the app opens the home screen
                signUpWithEmailUseCase(email = email, password = password)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: EmailAlreadyInUseException) {
                _uiState.update { uiState ->
                    uiState.copy(emailError = AccountError.EMAIL_ALREADY_IN_USE)
                }
            } catch (exception: InvalidEmailException) {
                _uiState.update { uiState ->
                    uiState.copy(emailError = AccountError.INVALID_EMAIL)
                }
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to sign up" }

                _event.send(SignUpEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isSigningUp = false)
                }
            }
        }
    }

    data class SignUpUIState(
        // Shown under the email: an email already used, or not an email
        val emailError: AccountError? = null,
        val isSigningUp: Boolean = false,
    )

    sealed interface SignUpEvent {
        data class ShowError(val error: AccountError) : SignUpEvent
    }

    companion object {
        fun canSignUp(email: String, password: String): Boolean =
            email.isNotBlank() && PasswordPolicy.missingRules(password).isEmpty()
    }
}
