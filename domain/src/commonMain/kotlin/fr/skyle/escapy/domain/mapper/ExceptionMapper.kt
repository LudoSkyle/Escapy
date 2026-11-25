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
import fr.skyle.escapy.domain.validation.PasswordRule
import kotlinx.coroutines.CancellationException
import fr.skyle.escapy.data.exception.NetworkUnavailableException as DataNetworkUnavailableException

/** The exception of the domain for an exception of data; any other is left as is. */
internal fun Throwable.toDomainException(): Throwable =
    when (this) {
        is AuthFailureException -> when (failure) {
            AuthFailure.INVALID_CREDENTIALS -> InvalidCredentialsException(cause = this)
            AuthFailure.EMAIL_ALREADY_IN_USE -> EmailAlreadyInUseException(cause = this)
            AuthFailure.INVALID_EMAIL -> InvalidEmailException(cause = this)
            // Firebase checks a length only: the rule it enforces is the length of the policy
            AuthFailure.WEAK_PASSWORD -> WeakPasswordException(missingRules = setOf(PasswordRule.MIN_LENGTH), cause = this)
            AuthFailure.TOO_MANY_REQUESTS -> TooManyAttemptsException(cause = this)
            AuthFailure.RECENT_SIGN_IN_REQUIRED -> RecentSignInRequiredException(cause = this)
            AuthFailure.UNKNOWN -> AccountUnavailableException(cause = this)
        }
        is DataNetworkUnavailableException -> NetworkUnavailableException(cause = this)
        is FirebaseUnavailableException -> AccountUnavailableException(cause = this)
        is FunctionFailureException -> when (reason) {
            "USERNAME_INVALID" -> InvalidUsernameException(cause = this)
            "RECENT_SIGN_IN_REQUIRED" -> RecentSignInRequiredException(cause = this)
            else -> AccountUnavailableException(cause = this)
        }
        else -> this
    }

/** Runs a call to data, its exceptions turned into those of the domain. */
internal suspend fun <T> withDomainExceptions(block: suspend () -> T): T =
    try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        throw exception.toDomainException()
    }
