package fr.skyle.escapy.data.repository.auth

import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import fr.skyle.escapy.data.exception.AuthFailure
import fr.skyle.escapy.data.exception.AuthFailureException
import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.authCall
import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.firebase.FirebaseServices
import fr.skyle.escapy.data.repository.auth.api.AuthRepository

internal class AuthRepositoryImpl(
    private val firebaseProvider: FirebaseProvider,
) : AuthRepository {

    private fun services(): FirebaseServices =
        firebaseProvider.services() ?: throw FirebaseUnavailableException()

    override suspend fun signInWithEmail(email: String, password: String) {
        val auth = services().auth
        authCall { auth.signInWithEmailAndPassword(email = email.trim(), password = password) }
    }

    override suspend fun signUpWithEmail(email: String, password: String) {
        val auth = services().auth
        authCall { auth.createUserWithEmailAndPassword(email = email.trim(), password = password) }
    }

    override suspend fun signInWithGoogle(idToken: String) {
        val auth = services().auth
        authCall { auth.signInWithCredential(googleCredential(idToken)) }
    }

    override suspend fun signInAsGuest() {
        val auth = services().auth
        authCall { auth.signInAnonymously() }
    }

    override suspend fun sendPasswordReset(email: String) {
        val auth = services().auth
        try {
            authCall { auth.sendPasswordResetEmail(email = email.trim()) }
        } catch (exception: AuthFailureException) {
            // Without the protection of the project against enumeration, an unknown address is refused:
            // the screen never tells it apart from a sent email
            if (exception.failure != AuthFailure.INVALID_CREDENTIALS) {
                throw exception
            }
        }
    }

    override suspend fun signOut() {
        val auth = firebaseProvider.services()?.auth ?: return
        authCall { auth.signOut() }
    }

    override suspend fun linkGuestWithEmail(email: String, password: String) {
        val user = currentUser()
        authCall {
            user.linkWithCredential(EmailAuthProvider.credential(email = email.trim(), password = password))
            user.sendEmailVerification()
        }
    }

    override suspend fun linkGuestWithGoogle(idToken: String) {
        val user = currentUser()
        authCall { user.linkWithCredential(googleCredential(idToken)) }
    }

    override suspend fun reauthenticate(password: String) {
        val user = currentUser()
        authCall {
            // The email of the account, as it is now: it may have changed on another device
            user.reload()
            val email = user.email ?: throw AuthFailureException(failure = AuthFailure.UNKNOWN)
            user.reauthenticate(EmailAuthProvider.credential(email = email, password = password))
            // A token of this new sign-in, for the functions that check its time
            user.getIdToken(forceRefresh = true)
        }
    }

    override suspend fun reauthenticateWithGoogle(idToken: String) {
        val user = currentUser()
        authCall {
            user.reauthenticate(googleCredential(idToken))
            // A token of this new sign-in, for the functions that check its time
            user.getIdToken(forceRefresh = true)
        }
    }

    override suspend fun changeEmail(newEmail: String) {
        val user = currentUser()
        authCall { user.verifyBeforeUpdateEmail(newEmail = newEmail.trim()) }
    }

    override suspend fun changePassword(newPassword: String) {
        val user = currentUser()
        authCall { user.updatePassword(password = newPassword) }
    }

    // The ID token is enough: Firebase checks it, and the app asks Google for no access to any API
    private fun googleCredential(idToken: String) =
        GoogleAuthProvider.credential(idToken = idToken, accessToken = null)

    private fun currentUser(): FirebaseUser =
        services().auth.currentUser ?: throw AuthFailureException(failure = AuthFailure.UNKNOWN)
}
