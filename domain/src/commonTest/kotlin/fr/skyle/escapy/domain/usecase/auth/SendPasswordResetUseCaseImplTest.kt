package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import fr.skyle.escapy.data.exception.NetworkUnavailableException as DataNetworkUnavailableException

class SendPasswordResetUseCaseImplTest {

    private val authRepository = FakeAuthRepository()

    private val useCase = SendPasswordResetUseCaseImpl(
        authRepository = authRepository,
    )

    @Test
    fun `the call reaches the repository`() = runTest {
        // When
        useCase(email = "player@escapy.app")

        // Then
        assertEquals(
            expected = listOf("reset:player@escapy.app"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `a failure becomes an exception of the domain`() = runTest {
        // Given
        authRepository.failure = DataNetworkUnavailableException()

        // When / Then
        assertFailsWith<NetworkUnavailableException> {
            useCase(email = "player@escapy.app")
        }
    }
}
