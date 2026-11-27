package fr.skyle.escapy.domain.mapper

import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.AccountExistsWithPasswordException
import fr.skyle.escapy.domain.exception.AccountUnavailableException
import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.ExcludedFromGameException
import fr.skyle.escapy.domain.exception.GameFinishedException
import fr.skyle.escapy.domain.exception.GameFullException
import fr.skyle.escapy.domain.exception.GameNotFoundException
import fr.skyle.escapy.domain.exception.GameNotJoinableException
import fr.skyle.escapy.domain.exception.GameNotStartedException
import fr.skyle.escapy.domain.exception.GoogleAccountAlreadyInUseException
import fr.skyle.escapy.domain.exception.InvalidAddedTimeException
import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.domain.exception.InvalidEmailException
import fr.skyle.escapy.domain.exception.InvalidGameTitleException
import fr.skyle.escapy.domain.exception.InvalidJoinCodeException
import fr.skyle.escapy.domain.exception.InvalidUsernameException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.exception.NotGameMasterException
import fr.skyle.escapy.domain.exception.RecentSignInRequiredException
import fr.skyle.escapy.domain.exception.TooManyAttemptsException
import fr.skyle.escapy.domain.exception.TooManyGamesException
import fr.skyle.escapy.domain.exception.WeakPasswordException
import fr.skyle.escapy.domain.exception.WrongGoogleAccountException
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
            AuthFailure.CREDENTIAL_ALREADY_IN_USE -> GoogleAccountAlreadyInUseException(cause = this)
            AuthFailure.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL -> AccountExistsWithPasswordException(cause = this)
            AuthFailure.USER_MISMATCH -> WrongGoogleAccountException(cause = this)
            AuthFailure.UNKNOWN -> AccountUnavailableException(cause = this)
        }
        is DataNetworkUnavailableException -> NetworkUnavailableException(cause = this)
        is FirebaseUnavailableException -> AccountUnavailableException(cause = this)
        is FunctionFailureException -> when (reason) {
            "USERNAME_INVALID" -> InvalidUsernameException(cause = this)
            "RECENT_SIGN_IN_REQUIRED" -> RecentSignInRequiredException(cause = this)
            "GAME_TITLE_INVALID" -> InvalidGameTitleException(cause = this)
            "TOO_MANY_GAMES" -> TooManyGamesException(cause = this)
            "JOIN_CODE_INVALID" -> InvalidJoinCodeException(cause = this)
            "TOO_MANY_ATTEMPTS" -> TooManyAttemptsException(cause = this)
            "GAME_NOT_FOUND" -> GameNotFoundException(cause = this)
            "GAME_NOT_JOINABLE" -> GameNotJoinableException(cause = this)
            "GAME_FULL" -> GameFullException(cause = this)
            "PLAYER_EXCLUDED" -> ExcludedFromGameException(cause = this)
            "NOT_GAME_MASTER" -> NotGameMasterException(cause = this)
            // Only a former member reaches a game they know the id of: they were excluded
            "NOT_GAME_MEMBER" -> ExcludedFromGameException(cause = this)
            "GAME_NOT_STARTED" -> GameNotStartedException(cause = this)
            "GAME_FINISHED" -> GameFinishedException(cause = this)
            "ADDED_TIME_INVALID" -> InvalidAddedTimeException(cause = this)
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
