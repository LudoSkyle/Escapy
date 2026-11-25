package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.WeakPasswordException
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import fr.skyle.escapy.domain.validation.PasswordRule
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SignUpWithEmailUseCaseImplTest {

    private val authRepository = FakeAuthRepository()

    private val signUpWithEmailUseCase = SignUpWithEmailUseCaseImpl(
        authRepository = authRepository,
    )

    @Test
    fun `a password that breaks the policy is refused before any call`() = runTest {
        // When
        val exception = assertFailsWith<WeakPasswordException> {
            signUpWithEmailUseCase(email = "player@escapy.app", password = "escapy")
        }

        // Then
        assertEquals(
            expected = setOf(PasswordRule.MIN_LENGTH, PasswordRule.UPPERCASE, PasswordRule.DIGIT, PasswordRule.SPECIAL_CHARACTER),
            actual = exception.missingRules,
        )
        assertEquals(
            expected = emptyList(),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `a valid password creates the account`() = runTest {
        // When
        signUpWithEmailUseCase(email = "player@escapy.app", password = "Escapy-2026")

        // Then
        assertEquals(
            expected = listOf("signUp:player@escapy.app"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `an email already used is told apart`() = runTest {
        // Given
        authRepository.failure = AuthFailureException(failure = AuthFailure.EMAIL_ALREADY_IN_USE)

        // When / Then
        assertFailsWith<EmailAlreadyInUseException> {
            signUpWithEmailUseCase(email = "player@escapy.app", password = "Escapy-2026")
        }
    }
}
