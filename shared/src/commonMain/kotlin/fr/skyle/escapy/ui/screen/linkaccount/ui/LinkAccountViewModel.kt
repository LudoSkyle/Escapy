package fr.skyle.escapy.ui.screen.linkaccount.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.InvalidEmailException
import fr.skyle.escapy.domain.usecase.account.LinkGuestAccountUseCase
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

/** Gives the guest account an email and a password: everything in it is kept. */
class LinkAccountViewModel(
    private val linkGuestAccountUseCase: LinkGuestAccountUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkAccountUIState())
    val uiState: StateFlow<LinkAccountUIState> = _uiState.asStateFlow()

    private val _event = Channel<LinkAccountEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun onEmailEdited() {
        _uiState.update { uiState ->
            uiState.copy(emailError = null)
        }
    }

    // The text being written stays in the screen, where each key is shown at once: given on sending
    fun onLinkClicked(email: String, password: String) {
        if (!canLink(email = email, password = password) || _uiState.value.isLinking) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isLinking = true)
            }

            try {
                linkGuestAccountUseCase(email = email, password = password)

                _uiState.update { uiState ->
                    uiState.copy(linkedTo = email.trim())
                }
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
                Logger.w(throwable = exception) { "Failed to link the guest account" }

                _event.send(LinkAccountEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isLinking = false)
                }
            }
        }
    }

    data class LinkAccountUIState(
        val emailError: AccountError? = null,
        val isLinking: Boolean = false,
        // The email the account is linked to, once linked: the screen says a verification email left
        val linkedTo: String? = null,
    )

    sealed interface LinkAccountEvent {
        data class ShowError(val error: AccountError) : LinkAccountEvent
    }

    companion object {
        fun canLink(email: String, password: String): Boolean =
            email.isNotBlank() && PasswordPolicy.missingRules(password).isEmpty()
    }
}
