package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.AccountUnavailableException
import fr.skyle.escapy.domain.fake.FakeProfileRepository
import fr.skyle.escapy.domain.model.Profile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnsureProfileUseCaseImplTest {

    private val profileRepository = FakeProfileRepository()

    private val ensureProfileUseCase = EnsureProfileUseCaseImpl(
        profileRepository = profileRepository,
    )

    @Test
    fun `the profile created by the server is returned`() = runTest {
        // When
        val profile = ensureProfileUseCase()

        // Then
        assertEquals(
            expected = Profile(
                username = "Player_ABC234",
                avatarId = 7,
            ),
            actual = profile,
        )
    }

    @Test
    fun `a refused call makes the account unavailable`() = runTest {
        // Given
        profileRepository.ensureFailure = FunctionFailureException(reason = "UNAUTHENTICATED")

        // When / Then
        assertFailsWith<AccountUnavailableException> {
            ensureProfileUseCase()
        }
    }
}
