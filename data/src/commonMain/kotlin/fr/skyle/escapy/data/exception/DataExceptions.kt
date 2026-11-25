package fr.skyle.escapy.data.exception

/** Why Firebase Auth refused, whatever the platform: both throw the same codes. */
enum class AuthFailure {
    // Wrong password, unknown email, or both, which the server may not tell apart on purpose
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    TOO_MANY_REQUESTS,
    RECENT_SIGN_IN_REQUIRED,
    UNKNOWN,
}

/** Firebase Auth refused a call. */
class AuthFailureException(
    val failure: AuthFailure,
    cause: Throwable? = null,
) : Exception("Firebase Auth refused: $failure", cause)

/** A Cloud Function refused a call, for the [reason] it sent (see docs/Backend/README.md). */
class FunctionFailureException(
    val reason: String?,
    cause: Throwable? = null,
) : Exception("A Cloud Function refused: ${reason ?: "no reason"}", cause)

/** The server could not be reached. */
class NetworkUnavailableException(
    cause: Throwable? = null,
) : Exception("The server could not be reached", cause)

/** Firebase did not start: a build without the private Firebase files. */
class FirebaseUnavailableException : Exception("Firebase did not start")
