package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.WeakPasswordException
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LinkGuestAccountUseCaseImplTest {

    private val authRepository = FakeAuthRepository()

    private val linkGuestAccountUseCase = LinkGuestAccountUseCaseImpl(
        authRepository = authRepository,
    )

    @Test
    fun `a weak password is refused before any call`() = runTest {
        assertFailsWith<WeakPasswordException> {
            linkGuestAccountUseCase(email = "guest@escapy.app", password = "escapy")
        }
        assertEquals(
            expected = emptyList(),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `the guest account is linked to the email`() = runTest {
        // When
        linkGuestAccountUseCase(email = "guest@escapy.app", password = "Escapy-2026")

        // Then
        assertEquals(
            expected = listOf("link:guest@escapy.app"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `an email used by another account is told apart`() = runTest {
        // Given
        authRepository.failure = AuthFailureException(failure = AuthFailure.EMAIL_ALREADY_IN_USE)

        // When / Then
        assertFailsWith<EmailAlreadyInUseException> {
            linkGuestAccountUseCase(email = "guest@escapy.app", password = "Escapy-2026")
        }
    }
}
