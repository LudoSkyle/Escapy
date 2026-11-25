package fr.skyle.escapy.ui.screen.welcome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.usecase.auth.SignInAsGuestUseCase
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUIState())
    val uiState: StateFlow<WelcomeUIState> = _uiState.asStateFlow()

    private val _event = Channel<WelcomeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun onContinueAsGuestClicked() {
        if (_uiState.value.isSigningInAsGuest) return
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
    )

    sealed interface WelcomeEvent {
        data class ShowError(val error: AccountError) : WelcomeEvent
    }
}
