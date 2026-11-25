package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.domain.fake.FakeProfileRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateAvatarUseCaseImplTest {

    private val profileRepository = FakeProfileRepository()

    private val updateAvatarUseCase = UpdateAvatarUseCaseImpl(
        profileRepository = profileRepository,
    )

    @Test
    fun `only the avatar is sent`() = runTest {
        // When
        updateAvatarUseCase(avatarId = 42)

        // Then
        assertEquals(
            expected = listOf<Pair<String?, Int?>>(null to 42),
            actual = profileRepository.updates,
        )
    }
}
