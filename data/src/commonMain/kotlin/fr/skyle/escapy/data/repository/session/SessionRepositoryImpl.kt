package fr.skyle.escapy.data.repository.session

import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class SessionRepositoryImpl(
    private val firebaseProvider: FirebaseProvider,
) : SessionRepository {

    override fun watchSession(): Flow<StoredSession> {
        val auth = firebaseProvider.services()?.auth ?: return flowOf(StoredSession.SignedOut)
        // The token changes on a sign-in, a sign-out, and when a guest links an email: the same user,
        // which authStateChanged would not tell
        return auth.idTokenChanged
            .map { user ->
                if (user == null) {
                    StoredSession.SignedOut
                } else {
                    StoredSession.SignedIn(
                        userId = user.uid,
                        isAnonymous = user.isAnonymous,
                        email = user.email,
                    )
                }
            }
            .distinctUntilChanged()
    }
}
