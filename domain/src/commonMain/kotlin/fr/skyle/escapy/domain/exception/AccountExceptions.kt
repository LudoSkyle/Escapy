package fr.skyle.escapy.domain.exception

import fr.skyle.escapy.domain.validation.PasswordRule

/** The email and the password do not match an account, whichever is wrong. */
class InvalidCredentialsException(cause: Throwable? = null) : Exception("Invalid credentials", cause)

/** An account already uses this email. */
class EmailAlreadyInUseException(cause: Throwable? = null) : Exception("Email already in use", cause)

/** The email is not an email address. */
class InvalidEmailException(cause: Throwable? = null) : Exception("Invalid email", cause)

/** The password breaks some of the [missingRules] of the policy of Escapy. */
class WeakPasswordException(
    val missingRules: Set<PasswordRule>,
    cause: Throwable? = null,
) : Exception("Weak password, missing $missingRules", cause)

/** Too many attempts in a short time: the server blocks them for a while. */
class TooManyAttemptsException(cause: Throwable? = null) : Exception("Too many attempts", cause)

/** The Google account already belongs to another account of Escapy: a guest cannot be linked to it. */
class GoogleAccountAlreadyInUseException(cause: Throwable? = null) : Exception("Google account already in use", cause)

/** The email of the Google account already has an account of Escapy, which signs in with a password. */
class AccountExistsWithPasswordException(cause: Throwable? = null) : Exception("Account exists with a password", cause)

/** Signed in again with another Google account than the one of the session. */
class WrongGoogleAccountException(cause: Throwable? = null) : Exception("Wrong Google account", cause)

/** The server could not be reached: no network, or the server is down. */
class NetworkUnavailableException(cause: Throwable? = null) : Exception("Network unavailable", cause)

/** The server asks a member to sign in again before this action: their sign-in is too old. */
class RecentSignInRequiredException(cause: Throwable? = null) : Exception("Recent sign-in required", cause)

/** The username is too short, too long, or has a forbidden character. */
class InvalidUsernameException(cause: Throwable? = null) : Exception("Invalid username", cause)

/** The account cannot be used: a build without Firebase, or a refusal the app does not know. */
class AccountUnavailableException(cause: Throwable? = null) : Exception("Account unavailable", cause)
