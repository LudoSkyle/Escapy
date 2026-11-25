package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.domain.exception.WeakPasswordException
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChangePasswordUseCaseImplTest {

    private val authRepository = FakeAuthRepository()

    private val changePasswordUseCase = ChangePasswordUseCaseImpl(
        authRepository = authRepository,
    )

    @Test
    fun `a weak new password is refused before any call`() = runTest {
        assertFailsWith<WeakPasswordException> {
            changePasswordUseCase(currentPassword = "Escapy-2026", newPassword = "short")
        }
        assertEquals(
            expected = emptyList(),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `the member signs in again then the password changes`() = runTest {
        // When
        changePasswordUseCase(currentPassword = "Escapy-2026", newPassword = "Escapy-2027")

        // Then
        assertEquals(
            expected = listOf("reauthenticate", "changePassword"),
            actual = authRepository.calls,
        )
    }
}
