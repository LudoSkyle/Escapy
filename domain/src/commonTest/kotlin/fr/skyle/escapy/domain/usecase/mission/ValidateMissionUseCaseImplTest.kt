package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.domain.fake.FakeMissionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidateMissionUseCaseImplTest {

    private val missionRepository = FakeMissionRepository()

    @Test
    fun `validating reaches the server`() = runTest {
        // When
        ValidateMissionUseCaseImpl(missionRepository = missionRepository)(gameId = "game", missionId = "safe")

        // Then
        assertEquals(expected = listOf("validate:safe"), actual = missionRepository.calls)
    }
}
