package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.domain.exception.GoogleAccountAlreadyInUseException
import fr.skyle.escapy.domain.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LinkGuestWithGoogleUseCaseImplTest {

    private val authRepository = FakeAuthRepository()

    private val linkGuestWithGoogleUseCase = LinkGuestWithGoogleUseCaseImpl(
        authRepository = authRepository,
    )

    @Test
    fun `the guest account is linked to Google`() = runTest {
        // When
        linkGuestWithGoogleUseCase(idToken = "token")

        // Then
        assertEquals(
            expected = listOf("linkGoogle:token"),
            actual = authRepository.calls,
        )
    }

    @Test
    fun `a Google account that already has an account is told`() = runTest {
        // Given
        authRepository.failure = AuthFailureException(failure = AuthFailure.CREDENTIAL_ALREADY_IN_USE)

        // When / Then
        assertFailsWith<GoogleAccountAlreadyInUseException> {
            linkGuestWithGoogleUseCase(idToken = "token")
        }
    }
}
