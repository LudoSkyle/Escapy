package fr.skyle.escapy.ui.screen.changepassword.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.domain.usecase.account.ChangePasswordUseCase
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

/** Sets a new password, after the current one. */
class ChangePasswordViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUIState())
    val uiState: StateFlow<ChangePasswordUIState> = _uiState.asStateFlow()

    private val _event = Channel<ChangePasswordEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun onCurrentPasswordEdited() {
        _uiState.update { uiState ->
            uiState.copy(currentPasswordError = null)
        }
    }

    // The text being written stays in the screen, where each key is shown at once: given on sending
    fun onChangeClicked(currentPassword: String, newPassword: String) {
        if (!canChange(currentPassword = currentPassword, newPassword = newPassword) || _uiState.value.isChanging) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isChanging = true)
            }

            try {
                changePasswordUseCase(currentPassword = currentPassword, newPassword = newPassword)

                _uiState.update { uiState ->
                    uiState.copy(isChanged = true)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: InvalidCredentialsException) {
                _uiState.update { uiState ->
                    uiState.copy(currentPasswordError = AccountError.INVALID_CREDENTIALS)
                }
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to change the password" }

                _event.send(ChangePasswordEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isChanging = false)
                }
            }
        }
    }

    data class ChangePasswordUIState(
        val currentPasswordError: AccountError? = null,
        val isChanging: Boolean = false,
        val isChanged: Boolean = false,
    )

    sealed interface ChangePasswordEvent {
        data class ShowError(val error: AccountError) : ChangePasswordEvent
    }

    companion object {
        fun canChange(currentPassword: String, newPassword: String): Boolean =
            currentPassword.isNotEmpty() && PasswordPolicy.missingRules(newPassword).isEmpty()
    }
}
