package fr.skyle.escapy.domain.mapper

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.AccountUnavailableException
import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.domain.exception.InvalidEmailException
import fr.skyle.escapy.domain.exception.InvalidUsernameException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.exception.RecentSignInRequiredException
import fr.skyle.escapy.domain.exception.TooManyAttemptsException
import fr.skyle.escapy.domain.exception.WeakPasswordException
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame
import fr.skyle.escapy.data.exception.NetworkUnavailableException as DataNetworkUnavailableException

class ExceptionMapperTest {

    @Test
    fun `each failure of Firebase Auth has its exception of the domain`() {
        assertIs<InvalidCredentialsException>(AuthFailureException(AuthFailure.INVALID_CREDENTIALS).toDomainException())
        assertIs<EmailAlreadyInUseException>(AuthFailureException(AuthFailure.EMAIL_ALREADY_IN_USE).toDomainException())
        assertIs<InvalidEmailException>(AuthFailureException(AuthFailure.INVALID_EMAIL).toDomainException())
        assertIs<WeakPasswordException>(AuthFailureException(AuthFailure.WEAK_PASSWORD).toDomainException())
        assertIs<TooManyAttemptsException>(AuthFailureException(AuthFailure.TOO_MANY_REQUESTS).toDomainException())
        assertIs<AccountUnavailableException>(AuthFailureException(AuthFailure.UNKNOWN).toDomainException())
    }

    @Test
    fun `a network failure a missing Firebase and a refused call have their exceptions`() {
        assertIs<NetworkUnavailableException>(DataNetworkUnavailableException().toDomainException())
        assertIs<AccountUnavailableException>(FirebaseUnavailableException().toDomainException())
        assertIs<AccountUnavailableException>(FunctionFailureException(reason = "SOMETHING_NEW").toDomainException())
    }

    @Test
    fun `the reasons of the functions and a too old sign-in have their exceptions`() {
        assertIs<InvalidUsernameException>(FunctionFailureException(reason = "USERNAME_INVALID").toDomainException())
        assertIs<RecentSignInRequiredException>(FunctionFailureException(reason = "RECENT_SIGN_IN_REQUIRED").toDomainException())
        assertIs<RecentSignInRequiredException>(AuthFailureException(AuthFailure.RECENT_SIGN_IN_REQUIRED).toDomainException())
    }

    @Test
    fun `any other exception is left as is`() {
        val exception = IllegalStateException("Unexpected")
        assertSame(
            expected = exception,
            actual = exception.toDomainException(),
        )
    }
}
