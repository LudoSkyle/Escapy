package fr.skyle.escapy.domain.model

/** Who uses the app: nobody yet, a guest, or a member with an account. */
sealed interface Session {

    data object SignedOut : Session

    data class SignedIn(
        val userId: String,
        // A guest plays without an email, and may link an account later
        val isGuest: Boolean,
        // Null for a guest
        val email: String? = null,
        // A member who signs in with Google only has none: no password to change, nor to confirm with
        val hasPassword: Boolean = false,
    ) : Session
}
