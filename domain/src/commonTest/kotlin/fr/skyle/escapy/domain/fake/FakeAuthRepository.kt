package fr.skyle.escapy.domain.fake

import fr.skyle.escapy.data.repository.auth.api.AuthRepository

class FakeAuthRepository : AuthRepository {

    /** Thrown by every call when set. */
    var failure: Exception? = null

    val calls = mutableListOf<String>()

    private fun record(call: String) {
        calls += call
        failure?.let { exception -> throw exception }
    }

    override suspend fun signInWithEmail(email: String, password: String) = record("signIn:$email")

    override suspend fun signUpWithEmail(email: String, password: String) = record("signUp:$email")

    override suspend fun signInAsGuest() = record("guest")

    override suspend fun sendPasswordReset(email: String) = record("reset:$email")

    override suspend fun signOut() = record("signOut")

    override suspend fun linkGuestWithEmail(email: String, password: String) = record("link:$email")

    override suspend fun reauthenticate(password: String) = record("reauthenticate")

    override suspend fun changeEmail(newEmail: String) = record("changeEmail:$newEmail")

    override suspend fun changePassword(newPassword: String) = record("changePassword")
}
