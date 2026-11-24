package fr.skyle.escapy.data.repository.session

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import fr.skyle.escapy.data.firebase.FirebaseStatus
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class SessionRepositoryImpl(
    private val firebaseStatus: FirebaseStatus,
) : SessionRepository {

    override fun watchSession(): Flow<StoredSession> {
        if (!firebaseStatus.isConfigured()) {
            return flowOf(StoredSession.SignedOut)
        }
        return Firebase.auth.authStateChanged
            .map { user ->
                if (user == null) {
                    StoredSession.SignedOut
                } else {
                    StoredSession.SignedIn(userId = user.uid, isAnonymous = user.isAnonymous)
                }
            }
            .distinctUntilChanged()
    }
}
