package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.InvalidMissionException
import fr.skyle.escapy.domain.exception.TooManyMissionsException
import fr.skyle.escapy.domain.fake.FakeMissionRepository
import fr.skyle.escapy.domain.model.MissionDraft
import fr.skyle.escapy.domain.model.MissionTrigger
import fr.skyle.escapy.domain.model.MissionType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SaveMissionUseCaseImplTest {

    private val missionRepository = FakeMissionRepository()

    private val saveMissionUseCase = SaveMissionUseCaseImpl(missionRepository = missionRepository)

    private val draft = MissionDraft(
        missionId = null,
        title = " Le mot ",
        statement = "",
        type = MissionType.TEXT,
        answers = listOf(" Lumière ", "", "Lumière"),
        trigger = MissionTrigger.AfterMission(missionId = "safe"),
        isFinal = true,
    )

    @Test
    fun `a new mission gets an id and is sent cleaned`() = runTest {
        // When
        val missionId = saveMissionUseCase(gameId = "game", draft = draft)

        // Then
        assertTrue(missionId.matches(Regex("[A-Za-z0-9]{20}")))
        assertEquals(expected = listOf("save:$missionId:Le mot:TEXT:Lumière:AFTER_MISSION:safe:true"), actual = missionRepository.calls)
    }

    @Test
    fun `a mission changed keeps its id`() = runTest {
        // When
        val missionId = saveMissionUseCase(gameId = "game", draft = draft.copy(missionId = "word", trigger = MissionTrigger.Manual))

        // Then
        assertEquals(expected = "word", actual = missionId)
        assertEquals(expected = listOf("save:word:Le mot:TEXT:Lumière:MANUAL:null:true"), actual = missionRepository.calls)
    }

    @Test
    fun `an invalid mission is refused before reaching the server, and a game full of missions is told`() = runTest {
        // When Then
        assertFailsWith<InvalidMissionException> { saveMissionUseCase(gameId = "game", draft = draft.copy(title = "")) }
        assertTrue(missionRepository.calls.isEmpty())
        missionRepository.failure = FunctionFailureException(reason = "TOO_MANY_MISSIONS")
        assertFailsWith<TooManyMissionsException> { saveMissionUseCase(gameId = "game", draft = draft) }
    }
}
