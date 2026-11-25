package fr.skyle.escapy.data.repository.account

import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.authCall
import fr.skyle.escapy.data.exception.functionCall
import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.function.EmptyRequest
import fr.skyle.escapy.data.repository.account.api.AccountRepository

internal class AccountRepositoryImpl(
    private val firebaseProvider: FirebaseProvider,
) : AccountRepository {

    override suspend fun deleteAccount() {
        val services = firebaseProvider.services() ?: throw FirebaseUnavailableException()
        val callable = services.functions.httpsCallable(DELETE_ACCOUNT)
        functionCall { callable.invoke(EmptyRequest) }
        // The account is gone on the server, but its token stays valid on this device until it
        // expires: the session ends here, at once
        authCall { services.auth.signOut() }
    }

    private companion object {
        const val DELETE_ACCOUNT = "deleteAccount"
    }
}
