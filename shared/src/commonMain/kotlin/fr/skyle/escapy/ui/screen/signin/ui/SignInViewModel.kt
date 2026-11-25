package fr.skyle.escapy.ui.screen.signin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.usecase.auth.SignInWithEmailUseCase
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

class SignInViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUIState())
    val uiState: StateFlow<SignInUIState> = _uiState.asStateFlow()

    private val _event = Channel<SignInEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    // The text being written stays in the screen, where each key is shown at once: given on sending
    fun onSignInClicked(email: String, password: String) {
        // The key of the keyboard too, which the disabled button does not stop
        if (!canSignIn(email = email, password = password) || _uiState.value.isSigningIn) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isSigningIn = true)
            }

            try {
                // The session changes: the app opens the home screen
                signInWithEmailUseCase(email = email, password = password)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to sign in" }

                _event.send(SignInEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isSigningIn = false)
                }
            }
        }
    }

    data class SignInUIState(
        val isSigningIn: Boolean = false,
    )

    sealed interface SignInEvent {
        data class ShowError(val error: AccountError) : SignInEvent
    }

    companion object {
        fun canSignIn(email: String, password: String): Boolean =
            email.isNotBlank() && password.isNotEmpty()
    }
}
