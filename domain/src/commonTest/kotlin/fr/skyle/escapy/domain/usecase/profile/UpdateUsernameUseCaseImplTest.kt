package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.InvalidUsernameException
import fr.skyle.escapy.domain.fake.FakeProfileRepository
import fr.skyle.escapy.domain.model.Profile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateUsernameUseCaseImplTest {

    private val profileRepository = FakeProfileRepository()

    private val updateUsernameUseCase = UpdateUsernameUseCaseImpl(
        profileRepository = profileRepository,
    )

    @Test
    fun `only the username is sent`() = runTest {
        // When
        val profile = updateUsernameUseCase(username = "Énigme Master")

        // Then
        assertEquals(
            expected = listOf<Pair<String?, Int?>>("Énigme Master" to null),
            actual = profileRepository.updates,
        )
        assertEquals(
            expected = Profile(
                username = "Énigme Master",
                avatarId = 7,
            ),
            actual = profile,
        )
    }

    @Test
    fun `a username refused by the server is told`() = runTest {
        // Given
        profileRepository.updateFailure = FunctionFailureException(reason = "USERNAME_INVALID")

        // When / Then
        assertFailsWith<InvalidUsernameException> {
            updateUsernameUseCase(username = "<b>")
        }
    }
}
