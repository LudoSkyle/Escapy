package fr.skyle.escapy.data.repository.auth.api

/**
 * Signs in, up and out with Firebase Auth. A failure is an AuthFailureException, a
 * NetworkUnavailableException, or a FirebaseUnavailableException in a build without Firebase.
 */
interface AuthRepository {

    suspend fun signInWithEmail(email: String, password: String)

    suspend fun signUpWithEmail(email: String, password: String)

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

    /** Signs in again with the password, for a change of email or password, or a deletion. */
    suspend fun reauthenticate(password: String)

    /** Sends a link to [newEmail]: the address of the account changes once it is opened. */
    suspend fun changeEmail(newEmail: String)

    suspend fun changePassword(newPassword: String)
}
