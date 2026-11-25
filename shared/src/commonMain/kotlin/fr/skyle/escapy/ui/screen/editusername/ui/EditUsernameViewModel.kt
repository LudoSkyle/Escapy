package fr.skyle.escapy.ui.screen.editusername.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.exception.InvalidUsernameException
import fr.skyle.escapy.domain.usecase.profile.UpdateUsernameUseCase
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

/** A new username, checked by the server, then the screen closes. */
class EditUsernameViewModel(
    private val updateUsernameUseCase: UpdateUsernameUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditUsernameUIState())
    val uiState: StateFlow<EditUsernameUIState> = _uiState.asStateFlow()

    private val _event = Channel<EditUsernameEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun onUsernameEdited() {
        _uiState.update { uiState ->
            uiState.copy(usernameError = null)
        }
    }

    // The text being written stays in the screen, where each key is shown at once: given on saving
    fun onSaveClicked(username: String) {
        if (username.isBlank() || _uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isSaving = true)
            }

            try {
                updateUsernameUseCase(username = username)

                _event.send(EditUsernameEvent.Saved)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: InvalidUsernameException) {
                _uiState.update { uiState ->
                    uiState.copy(usernameError = AccountError.INVALID_USERNAME)
                }
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to change the username" }

                _event.send(EditUsernameEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isSaving = false)
                }
            }
        }
    }

    data class EditUsernameUIState(
        val usernameError: AccountError? = null,
        val isSaving: Boolean = false,
    )

    sealed interface EditUsernameEvent {
        data object Saved : EditUsernameEvent
        data class ShowError(val error: AccountError) : EditUsernameEvent
    }
}
