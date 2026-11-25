package fr.skyle.escapy.data.model

/** The account the app is signed in with, as Firebase Auth gives it. */
sealed interface StoredSession {

    data object SignedOut : StoredSession

    data class SignedIn(
        val userId: String,
        val isAnonymous: Boolean,
        // Null for a guest
        val email: String?,
        // False for a guest, and for an account that signs in with Google only
        val hasPassword: Boolean,
    ) : StoredSession
}
