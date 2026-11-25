package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.domain.exception.AccountExistsWithPasswordException
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SignInWithGoogleUseCaseImplTest {

    private val authRepository = FakeAuthRepository()

    private val signInWithGoogleUseCase = SignInWithGoogleUseCaseImpl(
        authRepository = authRepository,
    )

    @Test
    fun `the user signs in with the token of Google`() = runTest {
        // When
        signInWithGoogleUseCase(idToken = "token")

        // Then
        assertEquals(
            expected = listOf("google:token"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `an email that already has a password is told`() = runTest {
        // Given
        authRepository.failure = AuthFailureException(failure = AuthFailure.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL)

        // When / Then
        assertFailsWith<AccountExistsWithPasswordException> {
            signInWithGoogleUseCase(idToken = "token")
        }
    }
}
