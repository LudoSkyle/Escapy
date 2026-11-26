package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.RecentSignInRequiredException
import fr.skyle.escapy.domain.exception.WrongGoogleAccountException
import fr.skyle.escapy.domain.fake.FakeAccountRepository
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import fr.skyle.escapy.domain.model.GoogleTokens
import fr.skyle.escapy.domain.model.Reauthentication
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

    private val tokens = GoogleTokens(idToken = "token", accessToken = "access")

    @Test
    fun `a member signs in again before the deletion`() = runTest {
        // When
        deleteAccountUseCase(reauthentication = Reauthentication.WithPassword(password = "Escapy-2026"))

        // Then
        assertEquals(
            expected = listOf("reauthenticate", "deleteAccount"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `a member of Google signs in again with Google before the deletion`() = runTest {
        // When
        deleteAccountUseCase(reauthentication = Reauthentication.WithGoogle(tokens = tokens))

        // Then
        assertEquals(
            expected = listOf("reauthenticateGoogle:token:access", "deleteAccount"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `another Google account is refused before the deletion`() = runTest {
        // Given
        authRepository.failure = AuthFailureException(failure = AuthFailure.USER_MISMATCH)

        // When / Then
        assertFailsWith<WrongGoogleAccountException> {
            deleteAccountUseCase(reauthentication = Reauthentication.WithGoogle(tokens = tokens))
        }
        assertEquals(
            expected = listOf("reauthenticateGoogle:token:access"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `a guest is deleted without a password`() = runTest {
        // When
        deleteAccountUseCase(reauthentication = Reauthentication.None)

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
            deleteAccountUseCase(reauthentication = Reauthentication.None)
        }
    }
}
