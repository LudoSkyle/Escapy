package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChangeEmailUseCaseImplTest {

    private val authRepository = FakeAuthRepository()

    private val changeEmailUseCase = ChangeEmailUseCaseImpl(
        authRepository = authRepository,
    )

    @Test
    fun `the member signs in again then the link is sent`() = runTest {
        // When
        changeEmailUseCase(currentPassword = "Escapy-2026", newEmail = "new@escapy.app")

        // Then
        assertEquals(
            expected = listOf("reauthenticate", "changeEmail:new@escapy.app"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `a wrong current password is told`() = runTest {
        // Given
        authRepository.failure = AuthFailureException(failure = AuthFailure.INVALID_CREDENTIALS)

        // When / Then
        assertFailsWith<InvalidCredentialsException> {
            changeEmailUseCase(currentPassword = "Wrong-2026", newEmail = "new@escapy.app")
        }
    }
}
