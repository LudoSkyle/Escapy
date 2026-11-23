package fr.skyle.escapy.ui.screens.lobby.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.skyle.escapy.data.repository.lobby.api.LobbyRepository
import fr.skyle.escapy.data.usecase.lobby.WatchLobbyUseCase
import fr.skyle.escapy.data.vo.User
import fr.skyle.escapy.ui.main.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lobbyRepository: LobbyRepository,
    private val watchLobbyUseCase: WatchLobbyUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.LobbyRoute>()

    private val _state = MutableStateFlow<State>(State())
    val state: StateFlow<State> by lazy { _state.asStateFlow() }

    init {
        viewModelScope.launch {
            lobbyRepository.listenLobby(route.lobbyId)
        }

        viewModelScope.launch {
            watchLobbyUseCase(route.lobbyId)
                .filterNotNull()
                .collect { lobby ->
                    _state.update {
                        it.copy(
                            createdByName = lobby.createdByName,
                            duration = lobby.duration,
                        )
                    }
                }
        }
    }

    fun eventDelivered() {
        _state.update {
            it.copy(event = null)
        }
    }

    data class State(
        val createdByName: String = "",
        val duration: Long = 0L,
        val participants: List<User> = emptyList(),
        val event: HomeEvent? = null
    )

    sealed interface HomeEvent {
        data class Error(val message: String?) : HomeEvent
    }
}