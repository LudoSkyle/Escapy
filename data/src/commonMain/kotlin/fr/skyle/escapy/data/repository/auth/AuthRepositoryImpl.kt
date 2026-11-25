package fr.skyle.escapy.data.repository.auth

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
}
