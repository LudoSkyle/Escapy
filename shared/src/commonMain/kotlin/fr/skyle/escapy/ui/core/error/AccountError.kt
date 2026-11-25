package fr.skyle.escapy.ui.core.error

import androidx.compose.runtime.Composable
import fr.skyle.escapy.domain.exception.AccountExistsWithPasswordException
import fr.skyle.escapy.domain.exception.EmailAlreadyInUseException
import fr.skyle.escapy.domain.exception.GoogleAccountAlreadyInUseException
import fr.skyle.escapy.domain.exception.InvalidCredentialsException
import fr.skyle.escapy.domain.exception.InvalidEmailException
import fr.skyle.escapy.domain.exception.InvalidUsernameException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.exception.RecentSignInRequiredException
import fr.skyle.escapy.domain.exception.TooManyAttemptsException
import fr.skyle.escapy.domain.exception.WeakPasswordException
import fr.skyle.escapy.domain.exception.WrongGoogleAccountException
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.error_account_exists_with_password
import fr.skyle.escapy.resources.error_email_already_in_use
import fr.skyle.escapy.resources.error_google_account_already_in_use
import fr.skyle.escapy.resources.error_invalid_credentials
import fr.skyle.escapy.resources.error_invalid_email
import fr.skyle.escapy.resources.error_invalid_username
import fr.skyle.escapy.resources.error_network
import fr.skyle.escapy.resources.error_recent_sign_in_required
import fr.skyle.escapy.resources.error_too_many_attempts
import fr.skyle.escapy.resources.error_unknown
import fr.skyle.escapy.resources.error_weak_password
import fr.skyle.escapy.resources.error_wrong_google_account
import fr.skyle.escapy.resources.error_wrong_password
import org.jetbrains.compose.resources.stringResource

/** What went wrong with an account, as a screen tells it. */
enum class AccountError {
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    INVALID_EMAIL,
    WEAK_PASSWORD,
    TOO_MANY_ATTEMPTS,
    RECENT_SIGN_IN_REQUIRED,
    INVALID_USERNAME,
    GOOGLE_ACCOUNT_ALREADY_IN_USE,
    ACCOUNT_EXISTS_WITH_PASSWORD,
    WRONG_GOOGLE_ACCOUNT,
    NETWORK,
    UNKNOWN,
}

internal fun Throwable.toAccountError(): AccountError =
    when (this) {
        is InvalidCredentialsException -> AccountError.INVALID_CREDENTIALS
        is EmailAlreadyInUseException -> AccountError.EMAIL_ALREADY_IN_USE
        is InvalidEmailException -> AccountError.INVALID_EMAIL
        is WeakPasswordException -> AccountError.WEAK_PASSWORD
        is TooManyAttemptsException -> AccountError.TOO_MANY_ATTEMPTS
        is RecentSignInRequiredException -> AccountError.RECENT_SIGN_IN_REQUIRED
        is InvalidUsernameException -> AccountError.INVALID_USERNAME
        is GoogleAccountAlreadyInUseException -> AccountError.GOOGLE_ACCOUNT_ALREADY_IN_USE
        is AccountExistsWithPasswordException -> AccountError.ACCOUNT_EXISTS_WITH_PASSWORD
        is WrongGoogleAccountException -> AccountError.WRONG_GOOGLE_ACCOUNT
        is NetworkUnavailableException -> AccountError.NETWORK
        else -> AccountError.UNKNOWN
    }

@Composable
internal fun AccountError.message(): String =
    stringResource(
        when (this) {
            AccountError.INVALID_CREDENTIALS -> Res.string.error_invalid_credentials
            AccountError.EMAIL_ALREADY_IN_USE -> Res.string.error_email_already_in_use
            AccountError.INVALID_EMAIL -> Res.string.error_invalid_email
            AccountError.WEAK_PASSWORD -> Res.string.error_weak_password
            AccountError.TOO_MANY_ATTEMPTS -> Res.string.error_too_many_attempts
            AccountError.RECENT_SIGN_IN_REQUIRED -> Res.string.error_recent_sign_in_required
            AccountError.INVALID_USERNAME -> Res.string.error_invalid_username
            AccountError.GOOGLE_ACCOUNT_ALREADY_IN_USE -> Res.string.error_google_account_already_in_use
            AccountError.ACCOUNT_EXISTS_WITH_PASSWORD -> Res.string.error_account_exists_with_password
            AccountError.WRONG_GOOGLE_ACCOUNT -> Res.string.error_wrong_google_account
            AccountError.NETWORK -> Res.string.error_network
            AccountError.UNKNOWN -> Res.string.error_unknown
        },
    )

/**
 * The message of an error under the current password, asked alone to confirm who acts: there is no
 * email to be wrong about.
 */
@Composable
internal fun AccountError.currentPasswordMessage(): String =
    when (this) {
        AccountError.INVALID_CREDENTIALS -> stringResource(Res.string.error_wrong_password)
        else -> message()
    }

/** The message of each error, read in the composition, for a snackbar shown from a coroutine. */
@Composable
internal fun accountErrorMessages(): Map<AccountError, String> =
    AccountError.entries.associateWith { error -> error.message() }
