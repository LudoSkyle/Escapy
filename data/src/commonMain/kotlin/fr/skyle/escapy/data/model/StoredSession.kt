package fr.skyle.escapy.data.model

/** The account the app is signed in with, as Firebase Auth gives it. */
sealed interface StoredSession {

    data object SignedOut : StoredSession

    data class SignedIn(
        val userId: String,
        val isAnonymous: Boolean,
    ) : StoredSession
}
