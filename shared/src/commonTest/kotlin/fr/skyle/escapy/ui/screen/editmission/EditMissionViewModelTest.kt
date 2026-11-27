package fr.skyle.escapy.ui.screen.editmission

import fr.skyle.escapy.domain.exception.TooManyMissionsException
import fr.skyle.escapy.domain.model.MissionState
import fr.skyle.escapy.domain.model.MissionTrigger
import fr.skyle.escapy.domain.model.MissionType
import fr.skyle.escapy.fake.FakeDeleteMissionUseCase
import fr.skyle.escapy.fake.FakeSaveMissionUseCase
import fr.skyle.escapy.fake.FakeWatchMissionsUseCase
import fr.skyle.escapy.fake.mission
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.screen.editmission.ui.EditMissionViewModel
import fr.skyle.escapy.ui.screen.editmission.ui.EditMissionViewModel.EditMissionEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EditMissionViewModelTest : MainDispatcherTest() {

    private val watchMissionsUseCase = FakeWatchMissionsUseCase()
    private val saveMissionUseCase = FakeSaveMissionUseCase()
    private val deleteMissionUseCase = FakeDeleteMissionUseCase()

    private fun viewModel(missionId: String?) =
        EditMissionViewModel(
            gameId = "game",
            missionId = missionId,
            watchMissionsUseCase = watchMissionsUseCase,
            saveMissionUseCase = saveMissionUseCase,
            deleteMissionUseCase = deleteMissionUseCase,
        )

    @Test
    fun `a mission to change fills the form once and later changes do not erase the typing`() = runTest {
        // Given
        val viewModel = viewModel(missionId = "safe")
        watchMissionsUseCase.missions.emit(listOf(mission(id = "safe", trigger = MissionTrigger.OnStart, answers = listOf("0427"))))

        // When
        viewModel.onTitleChanged("Le coffre")
        watchMissionsUseCase.missions.emit(listOf(mission(id = "safe", trigger = MissionTrigger.OnStart, answers = listOf("9999"))))

        // Then
        assertEquals(expected = true, actual = watchMissionsUseCase.asMaster.single())
        assertEquals(expected = "Le coffre", actual = viewModel.uiState.value.title)
        assertEquals(expected = listOf("0427"), actual = viewModel.uiState.value.answers)
        assertEquals(expected = MissionTrigger.OnStart, actual = viewModel.uiState.value.trigger)
    }

    @Test
    fun `a mission may follow neither itself nor one that follows it`() = runTest {
        // Given
        val viewModel = viewModel(missionId = "a")

        // When: b follows a, c follows b, d stands alone
        watchMissionsUseCase.missions.emit(
            listOf(
                mission(id = "a", trigger = MissionTrigger.OnStart),
                mission(id = "b", trigger = MissionTrigger.AfterMission(missionId = "a")),
                mission(id = "c", trigger = MissionTrigger.AfterMission(missionId = "b")),
                mission(id = "d", trigger = MissionTrigger.Manual),
            ),
        )

        // Then
        assertEquals(expected = listOf("d"), actual = viewModel.uiState.value.followableMissions.map { mission -> mission.id })
    }

    @Test
    fun `a new mission is saved once valid and the screen closes`() = runTest {
        // Given
        val viewModel = viewModel(missionId = null)
        watchMissionsUseCase.missions.emit(emptyList())
        val canSaveEmpty = viewModel.uiState.value.canSave

        // When
        viewModel.onTitleChanged("Le mot")
        viewModel.onTypeChanged(MissionType.TEXT)
        viewModel.onAnswerChanged(index = 0, answer = "Lumière")
        viewModel.onAnswerAdded()
        viewModel.onAnswerChanged(index = 1, answer = "Lampe")
        viewModel.onFinalChanged(true)
        viewModel.onSaveClicked()

        // Then
        assertFalse(canSaveEmpty)
        val draft = saveMissionUseCase.drafts.single()
        assertEquals(expected = null, actual = draft.missionId)
        assertEquals(expected = listOf("Lumière", "Lampe"), actual = draft.answers)
        assertTrue(draft.isFinal)
        assertEquals(expected = EditMissionEvent.Closed, actual = viewModel.event.first())
    }

    @Test
    fun `a refusal of the server is told and keeps the screen`() = runTest {
        // Given
        val viewModel = viewModel(missionId = null)
        watchMissionsUseCase.missions.emit(emptyList())
        viewModel.onTitleChanged("Le coffre")
        viewModel.onAnswerChanged(index = 0, answer = "0427")
        saveMissionUseCase.failure = TooManyMissionsException()

        // When
        viewModel.onSaveClicked()

        // Then
        assertEquals(expected = EditMissionEvent.ShowError(error = GameError.TOO_MANY_MISSIONS), actual = viewModel.event.first())
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `a mission deleted closes the screen and one deleted elsewhere is told missing`() = runTest {
        // Given
        val viewModel = viewModel(missionId = "safe")
        watchMissionsUseCase.missions.emit(listOf(mission(id = "safe", state = MissionState.LOCKED)))

        // When
        viewModel.onDeleteConfirmed()
        watchMissionsUseCase.missions.emit(emptyList())

        // Then
        assertEquals(expected = listOf("safe"), actual = deleteMissionUseCase.calls)
        assertEquals(expected = EditMissionEvent.Closed, actual = viewModel.event.first())
        assertTrue(viewModel.uiState.value.isMissing)
    }
}
