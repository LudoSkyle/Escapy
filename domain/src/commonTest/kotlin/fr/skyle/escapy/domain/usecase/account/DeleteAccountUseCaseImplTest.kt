package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.RecentSignInRequiredException
import fr.skyle.escapy.domain.fake.FakeAccountRepository
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeleteAccountUseCaseImplTest {

    private val authRepository = FakeAuthRepository()
    private val accountRepository = FakeAccountRepository(calls = authRepository.calls)

    private val deleteAccountUseCase = DeleteAccountUseCaseImpl(
        authRepository = authRepository,
        accountRepository = accountRepository,
    )

    @Test
    fun `a member signs in again before the deletion`() = runTest {
        // When
        deleteAccountUseCase(currentPassword = "Escapy-2026")

        // Then
        assertEquals(
            expected = listOf("reauthenticate", "deleteAccount"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `a guest is deleted without a password`() = runTest {
        // When
        deleteAccountUseCase(currentPassword = null)

        // Then
        assertEquals(
            expected = listOf("deleteAccount"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `a too old sign-in refused by the server is told`() = runTest {
        // Given
        accountRepository.failure = FunctionFailureException(reason = "RECENT_SIGN_IN_REQUIRED")

        // When / Then
        assertFailsWith<RecentSignInRequiredException> {
            deleteAccountUseCase(currentPassword = null)
        }
    }
}
