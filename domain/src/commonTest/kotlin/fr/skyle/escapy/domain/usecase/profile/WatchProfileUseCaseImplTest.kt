package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.data.model.StoredProfile
import fr.skyle.escapy.domain.fake.FakeProfileRepository
import fr.skyle.escapy.domain.model.Profile
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WatchProfileUseCaseImplTest {

    private val profileRepository = FakeProfileRepository()

    private val watchProfileUseCase = WatchProfileUseCaseImpl(
        profileRepository = profileRepository,
    )

    @Test
    fun `a missing profile then a created one are followed`() = runTest {
        // Given
        profileRepository.profiles = flowOf(
            null,
            StoredProfile(
                username = "Player_ABC234",
                avatarId = 7,
            ),
        )

        // When
        val profiles = watchProfileUseCase(userId = "user").toList()

        // Then
        assertEquals(
            expected = listOf(
                null,
                Profile(
                    username = "Player_ABC234",
                    avatarId = 7,
                ),
            ),
            actual = profiles,
        )
    }
}
