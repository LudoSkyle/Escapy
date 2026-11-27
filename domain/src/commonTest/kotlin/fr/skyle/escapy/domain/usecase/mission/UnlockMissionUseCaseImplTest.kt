package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.domain.fake.FakeMissionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UnlockMissionUseCaseImplTest {

    private val missionRepository = FakeMissionRepository()

    @Test
    fun `unlocking reaches the server`() = runTest {
        // When
        UnlockMissionUseCaseImpl(missionRepository = missionRepository)(gameId = "game", missionId = "safe")

        // Then
        assertEquals(expected = listOf("unlock:safe"), actual = missionRepository.calls)
    }
}
