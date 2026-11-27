package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.data.exception.AccessDeniedException
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.domain.fake.FakeMissionRepository
import fr.skyle.escapy.domain.fake.FakeSessionRepository
import fr.skyle.escapy.domain.fake.storedMission
import fr.skyle.escapy.domain.model.MissionState
import fr.skyle.escapy.domain.model.MissionTrigger
import fr.skyle.escapy.domain.model.MissionType
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WatchMissionsUseCaseImplTest {

    private val missionRepository = FakeMissionRepository()

    private val watchMissionsUseCase = WatchMissionsUseCaseImpl(
        sessionRepository = FakeSessionRepository().apply {
            sessions = flowOf(StoredSession.SignedIn(userId = "player", isAnonymous = true, email = null, hasPassword = false))
        },
        missionRepository = missionRepository,
    )

    @Test
    fun `a player reads the missions they see and never the full ones`() = runTest {
        // Given
        missionRepository.missions = flowOf(listOf(storedMission(id = "safe", state = "SOLVED", solverId = "player")))

        // When
        val missions = watchMissionsUseCase(gameId = "game", isMaster = false).toList().single()

        // Then
        assertEquals(expected = listOf("watchMissions"), actual = missionRepository.calls)
        assertEquals(expected = MissionType.CODE, actual = missions.single().type)
        assertEquals(expected = MissionState.SOLVED, actual = missions.single().state)
        assertTrue(missions.single().solver!!.isCurrentUser)
        assertNull(missions.single().trigger)
    }

    @Test
    fun `the game master reads every mission with its trigger`() = runTest {
        // Given
        missionRepository.masterMissions = flowOf(
            listOf(storedMission(id = "next", state = "LOCKED", triggerType = "AFTER_MISSION", triggerMissionId = "safe")),
        )

        // When
        val missions = watchMissionsUseCase(gameId = "game", isMaster = true).toList().single()

        // Then
        assertEquals(expected = listOf("watchMasterMissions"), actual = missionRepository.calls)
        assertEquals(expected = MissionTrigger.AfterMission(missionId = "safe"), actual = missions.single().trigger)
        assertEquals(expected = listOf("0427"), actual = missions.single().answers)
    }

    @Test
    fun `missions that can no longer be read end without an error`() = runTest {
        // Given
        missionRepository.missions = flow {
            emit(listOf(storedMission(id = "safe")))
            throw AccessDeniedException()
        }

        // When
        val emissions = watchMissionsUseCase(gameId = "game", isMaster = false).toList()

        // Then
        assertEquals(expected = 1, actual = emissions.size)
    }
}
