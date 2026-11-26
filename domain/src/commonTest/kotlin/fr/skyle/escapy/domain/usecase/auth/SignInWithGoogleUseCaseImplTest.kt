package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.domain.exception.AccountExistsWithPasswordException
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import fr.skyle.escapy.domain.model.GoogleTokens
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SignInWithGoogleUseCaseImplTest {

    private val authRepository = FakeAuthRepository()

    private val signInWithGoogleUseCase = SignInWithGoogleUseCaseImpl(
        authRepository = authRepository,
    )

    private val tokens = GoogleTokens(idToken = "token", accessToken = "access")

    @Test
    fun `the user signs in with the tokens of Google`() = runTest {
        // When
        signInWithGoogleUseCase(tokens = tokens)

        // Then
        assertEquals(
            expected = listOf("google:token:access"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `an ID token without an access token is enough`() = runTest {
        // When
        signInWithGoogleUseCase(tokens = GoogleTokens(idToken = "token", accessToken = null))

        // Then
        assertEquals(
            expected = listOf("google:token:null"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `an email that already has a password is told`() = runTest {
        // Given
        authRepository.failure = AuthFailureException(failure = AuthFailure.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL)

        // When / Then
        assertFailsWith<AccountExistsWithPasswordException> {
            signInWithGoogleUseCase(tokens = tokens)
        }
    }
}
