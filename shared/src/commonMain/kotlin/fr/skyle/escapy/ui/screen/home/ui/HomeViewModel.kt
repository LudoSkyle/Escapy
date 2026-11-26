package fr.skyle.escapy.ui.screen.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.GameSummary
import fr.skyle.escapy.domain.usecase.game.JoinGameUseCase
import fr.skyle.escapy.domain.usecase.game.WatchMyGamesUseCase
import fr.skyle.escapy.domain.validation.GameRules
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.core.error.toGameError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The games of the user, live, and the way into a game: its code, or a new one. */
class HomeViewModel(
    private val watchMyGamesUseCase: WatchMyGamesUseCase,
    private val joinGameUseCase: JoinGameUseCase,
) : ViewModel() {

    // What the user does here; the games come from their own observation
    private val _uiState = MutableStateFlow(HomeUIState())

    /**
     * The games are observed only while the screen is shown, and read again when it shows again: the
     * Android SDK may leave a game the user just lost access to in an observation that stays open
     * meanwhile, until its next change (see docs/KMP/01_MODULE_DATA.md).
     */
    private val games: StateFlow<List<GameSummary>?> = watchMyGamesUseCase()
        .catch { exception ->
            Logger.e(throwable = exception) { "Failed to read the games" }
            emit(emptyList())
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = null)

    val uiState: StateFlow<HomeUIState> = combine(_uiState, games) { uiState, games ->
        uiState.copy(games = games)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = HomeUIState())

    private val _event = Channel<HomeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    fun onJoinCodeEdited() {
        _uiState.update { uiState ->
            uiState.copy(joinCodeError = null)
        }
    }

    // The code being typed stays in the screen, where each key is shown at once: given on joining
    fun onJoinClicked(code: String) {
        if (!GameRules.isJoinCodeComplete(code) || _uiState.value.isJoining) return
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isJoining = true)
            }

            try {
                val gameId = joinGameUseCase(code = code)

                _event.send(HomeEvent.OpenGame(gameId = gameId))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to join a game" }

                val error = exception.toGameError()
                if (error.isAboutJoinCode) {
                    _uiState.update { uiState ->
                        uiState.copy(joinCodeError = error)
                    }
                } else {
                    _event.send(HomeEvent.ShowError(error = error))
                }
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isJoining = false)
                }
            }
        }
    }

    data class HomeUIState(
        // Null until the first read
        val games: List<GameSummary>? = null,
        val joinCodeError: GameError? = null,
        val isJoining: Boolean = false,
    )

    sealed interface HomeEvent {
        data class OpenGame(val gameId: String) : HomeEvent
        data class ShowError(val error: GameError) : HomeEvent
    }
}
