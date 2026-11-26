package fr.skyle.escapy.data.repository.auth.api

/**
 * Signs in, up and out with Firebase Auth. A failure is an AuthFailureException, a
 * NetworkUnavailableException, or a FirebaseUnavailableException in a build without Firebase.
 */
interface AuthRepository {

    suspend fun signInWithEmail(email: String, password: String)

    suspend fun signUpWithEmail(email: String, password: String)

    /**
     * Signs in with the tokens Google gave the app, creating the account the first time. Throws
     * ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL when the email already has an account with a password.
     */
    suspend fun signInWithGoogle(idToken: String, accessToken: String?)

    /** A guest: an anonymous account, which may later be linked to an email. */
    suspend fun signInAsGuest()

    /** Sends the email to reset a password. Nothing tells whether the address has an account. */
    suspend fun sendPasswordReset(email: String)

    suspend fun signOut()

    /**
     * Turns the guest account into an account with an email and a password, keeping everything in
     * it, then sends the email to verify the address.
     */
    suspend fun linkGuestWithEmail(email: String, password: String)

    /**
     * Turns the guest account into an account that signs in with Google, keeping everything in it.
     * Throws CREDENTIAL_ALREADY_IN_USE when this Google account already has an account of Escapy.
     */
    suspend fun linkGuestWithGoogle(idToken: String, accessToken: String?)

    /** Signs in again with the password, for a change of email or password, or a deletion. */
    suspend fun reauthenticate(password: String)

    /** Signs in again with Google, for a deletion. Throws USER_MISMATCH for another Google account. */
    suspend fun reauthenticateWithGoogle(idToken: String, accessToken: String?)

    /** Sends a link to [newEmail]: the address of the account changes once it is opened. */
    suspend fun changeEmail(newEmail: String)

    suspend fun changePassword(newPassword: String)
}
