package fr.skyle.escapy.data.repository.session.api

import fr.skyle.escapy.data.model.StoredSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    /** The current session, then each change: sign-in, sign-out, account deleted elsewhere. */
    fun watchSession(): Flow<StoredSession>
}
