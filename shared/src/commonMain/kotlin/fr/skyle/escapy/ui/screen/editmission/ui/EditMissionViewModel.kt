package fr.skyle.escapy.ui.screen.editmission.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.escapy.domain.model.Mission
import fr.skyle.escapy.domain.model.MissionDraft
import fr.skyle.escapy.domain.model.MissionTrigger
import fr.skyle.escapy.domain.model.MissionType
import fr.skyle.escapy.domain.usecase.mission.DeleteMissionUseCase
import fr.skyle.escapy.domain.usecase.mission.SaveMissionUseCase
import fr.skyle.escapy.domain.usecase.mission.WatchMissionsUseCase
import fr.skyle.escapy.domain.validation.MissionRules
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.core.error.toGameError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A mission of a game as its game master writes it, new ([missionId] null) or to change, before the
 * game starts or while it is played. The other missions of the game are the ones it may follow.
 */
class EditMissionViewModel(
    private val gameId: String,
    private val missionId: String?,
    private val watchMissionsUseCase: WatchMissionsUseCase,
    private val saveMissionUseCase: SaveMissionUseCase,
    private val deleteMissionUseCase: DeleteMissionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditMissionUIState(isNew = missionId == null))
    val uiState: StateFlow<EditMissionUIState> = _uiState.asStateFlow()

    private val _event = Channel<EditMissionEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    // The form is filled from the mission once: later changes of the mission do not erase the typing
    private var isFilled = missionId == null

    init {
        viewModelScope.launch {
            watchMissionsUseCase(gameId = gameId, isMaster = true)
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the missions" }
                }
                .collect { missions ->
                    val mission = missions.firstOrNull { candidate -> candidate.id == missionId }
                    _uiState.update { uiState ->
                        val filled = if (!isFilled && mission != null) uiState.filledWith(mission) else uiState
                        filled.copy(
                            isLoaded = true,
                            isMissing = missionId != null && mission == null,
                            followableMissions = followable(missions = missions),
                        )
                    }
                    if (mission != null) isFilled = true
                }
        }
    }

    // The missions this one may follow: never itself, nor one that follows it, even through others
    private fun followable(missions: List<Mission>): List<Mission> {
        val triggers = missions.associate { mission -> mission.id to mission.trigger }
        fun followsThis(id: String): Boolean {
            var current: String? = id
            val seen = mutableSetOf<String>()
            while (current != null && seen.add(current)) {
                if (current == missionId) return true
                current = (triggers[current] as? MissionTrigger.AfterMission)?.missionId
            }
            return false
        }
        return missions.filter { mission -> mission.id != missionId && !followsThis(mission.id) }
    }

    fun onTitleChanged(title: String) = _uiState.update { uiState -> uiState.copy(title = title) }

    fun onStatementChanged(statement: String) = _uiState.update { uiState -> uiState.copy(statement = statement) }

    fun onTypeChanged(type: MissionType) = _uiState.update { uiState -> uiState.copy(type = type) }

    fun onAnswerChanged(index: Int, answer: String) = _uiState.update { uiState ->
        uiState.copy(answers = uiState.answers.mapIndexed { position, current -> if (position == index) answer else current })
    }

    fun onAnswerAdded() = _uiState.update { uiState ->
        if (uiState.answers.size >= MissionRules.ANSWERS_MAX) uiState else uiState.copy(answers = uiState.answers + "")
    }

    fun onAnswerRemoved(index: Int) = _uiState.update { uiState ->
        if (uiState.answers.size <= 1) uiState else uiState.copy(answers = uiState.answers.filterIndexed { position, _ -> position != index })
    }

    fun onTriggerChanged(trigger: MissionTrigger) = _uiState.update { uiState -> uiState.copy(trigger = trigger) }

    fun onFinalChanged(isFinal: Boolean) = _uiState.update { uiState -> uiState.copy(isFinal = isFinal) }

    fun onSaveClicked() {
        val uiState = _uiState.value
        if (!uiState.canSave) return
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isSaving = true) }
            try {
                saveMissionUseCase(gameId = gameId, draft = uiState.draft(missionId = missionId))

                _event.send(EditMissionEvent.Closed)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to save a mission" }

                _event.send(EditMissionEvent.ShowError(error = exception.toGameError()))
            } finally {
                _uiState.update { current -> current.copy(isSaving = false) }
            }
        }
    }

    fun onDeleteConfirmed() {
        val id = missionId ?: return
        if (_uiState.value.isDeleting) return
        viewModelScope.launch {
            _uiState.update { current -> current.copy(isDeleting = true) }
            try {
                deleteMissionUseCase(gameId = gameId, missionId = id)

                _event.send(EditMissionEvent.Closed)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.w(throwable = exception) { "Failed to delete a mission" }

                _event.send(EditMissionEvent.ShowError(error = exception.toGameError()))
            } finally {
                _uiState.update { current -> current.copy(isDeleting = false) }
                _event.send(EditMissionEvent.ActionFinished)
            }
        }
    }

    data class EditMissionUIState(
        val isNew: Boolean,
        // False until the missions of the game are read
        val isLoaded: Boolean = false,
        // The mission was deleted meanwhile
        val isMissing: Boolean = false,
        val title: String = "",
        val statement: String = "",
        val type: MissionType = MissionType.CODE,
        val answers: List<String> = listOf(""),
        // A new mission shows at the start: the most usual
        val trigger: MissionTrigger = MissionTrigger.OnStart,
        val isFinal: Boolean = false,
        val followableMissions: List<Mission> = emptyList(),
        val isSaving: Boolean = false,
        val isDeleting: Boolean = false,
    ) {
        fun draft(missionId: String?): MissionDraft =
            MissionDraft(
                missionId = missionId,
                title = title,
                statement = statement,
                type = type,
                answers = answers,
                trigger = trigger,
                isFinal = isFinal,
            )

        val canSave: Boolean
            get() = isLoaded && !isMissing && !isSaving && !isDeleting && MissionRules.isValid(draft(missionId = null))

        fun filledWith(mission: Mission): EditMissionUIState =
            copy(
                title = mission.title,
                statement = mission.statement,
                type = mission.type,
                answers = mission.answers.ifEmpty { listOf("") },
                trigger = mission.trigger ?: MissionTrigger.Manual,
                isFinal = mission.isFinal,
            )
    }

    sealed interface EditMissionEvent {
        data object Closed : EditMissionEvent

        /** The deletion is over, done or failed: its dialog closes. */
        data object ActionFinished : EditMissionEvent

        data class ShowError(val error: GameError) : EditMissionEvent
    }
}
