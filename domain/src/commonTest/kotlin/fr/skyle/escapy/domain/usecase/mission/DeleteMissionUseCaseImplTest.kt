package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.domain.fake.FakeMissionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteMissionUseCaseImplTest {

    private val missionRepository = FakeMissionRepository()

    @Test
    fun `deleting reaches the server`() = runTest {
        // When
        DeleteMissionUseCaseImpl(missionRepository = missionRepository)(gameId = "game", missionId = "safe")

        // Then
        assertEquals(expected = listOf("delete:safe"), actual = missionRepository.calls)
    }
}
