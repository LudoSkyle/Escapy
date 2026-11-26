package fr.skyle.escapy.ui.screen.welcome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.GoogleTokens
import fr.skyle.escapy.domain.usecase.auth.SignInAsGuestUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInWithGoogleUseCase
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

class WelcomeViewModel(
    private val signInAsGuestUseCase: SignInAsGuestUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUIState())
    val uiState: StateFlow<WelcomeUIState> = _uiState.asStateFlow()

    private val _event = Channel<WelcomeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    // The Route asks Google for the tokens natively, then gives it here
    fun onGoogleTokensReceived(tokens: GoogleTokens) {
        if (_uiState.value.isSigningIn) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isSigningInWithGoogle = true)
            }

            try {
                // The session changes: the app opens the home screen, or creates the profile first
                signInWithGoogleUseCase(tokens = tokens)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to sign in with Google" }

                _event.send(WelcomeEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isSigningInWithGoogle = false)
                }
            }
        }
    }

    fun onGoogleSignInFailed(exception: Exception) {
        Logger.e(throwable = exception) { "Google gave no token" }

        viewModelScope.launch {
            _event.send(WelcomeEvent.ShowError(error = AccountError.UNKNOWN))
        }
    }

    fun onContinueAsGuestClicked() {
        if (_uiState.value.isSigningIn) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isSigningInAsGuest = true)
            }

            try {
                // The session changes: the app opens the home screen
                signInAsGuestUseCase()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to sign in as a guest" }

                _event.send(WelcomeEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isSigningInAsGuest = false)
                }
            }
        }
    }

    data class WelcomeUIState(
        val isSigningInAsGuest: Boolean = false,
        val isSigningInWithGoogle: Boolean = false,
    ) {
        val isSigningIn: Boolean
            get() = isSigningInAsGuest || isSigningInWithGoogle
    }

    sealed interface WelcomeEvent {
        data class ShowError(val error: AccountError) : WelcomeEvent
    }
}
