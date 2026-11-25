package fr.skyle.escapy.ui.screen.editavatar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.domain.usecase.profile.UpdateAvatarUseCase
import fr.skyle.escapy.domain.usecase.profile.WatchProfileUseCase
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import fr.skyle.escapy.ui.core.error.AccountError
import fr.skyle.escapy.ui.core.error.toAccountError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One of the avatars of the app, chosen: saved at once, then the screen closes. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditAvatarViewModel(
    private val watchSessionUseCase: WatchSessionUseCase,
    private val watchProfileUseCase: WatchProfileUseCase,
    private val updateAvatarUseCase: UpdateAvatarUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditAvatarUIState())
    val uiState: StateFlow<EditAvatarUIState> = _uiState.asStateFlow()

    private val _event = Channel<EditAvatarEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            watchSessionUseCase()
                .flatMapLatest { session ->
                    if (session is Session.SignedIn) watchProfileUseCase(userId = session.userId) else emptyFlow()
                }
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the profile" }
                }
                .collect { profile ->
                    _uiState.update { uiState ->
                        uiState.copy(currentAvatarId = profile?.avatarId)
                    }
                }
        }
    }

    fun onAvatarSelected(avatarId: Int) {
        if (_uiState.value.savingAvatarId != null) return
        if (avatarId == _uiState.value.currentAvatarId) {
            viewModelScope.launch { _event.send(EditAvatarEvent.Saved) }
            return
        }
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(savingAvatarId = avatarId)
            }

            try {
                updateAvatarUseCase(avatarId = avatarId)

                _event.send(EditAvatarEvent.Saved)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to change the avatar" }

                _event.send(EditAvatarEvent.ShowError(error = exception.toAccountError()))
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(savingAvatarId = null)
                }
            }
        }
    }

    data class EditAvatarUIState(
        val currentAvatarId: Int? = null,
        // The avatar being saved: shown as chosen, the others wait
        val savingAvatarId: Int? = null,
    )

    sealed interface EditAvatarEvent {
        data object Saved : EditAvatarEvent
        data class ShowError(val error: AccountError) : EditAvatarEvent
    }
}
