package fr.skyle.escapy.domain.model

/** How a user proves it is them again before a sensitive action, such as deleting the account. */
sealed interface Reauthentication {

    /** A guest has nothing to sign in again with: the server does not ask them. */
    data object None : Reauthentication

    data class WithPassword(val password: String) : Reauthentication

    /** With the ID token Google just gave the app. */
    data class WithGoogle(val idToken: String) : Reauthentication
}
