package fr.skyle.escapy.domain.fake

import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeSessionRepository : SessionRepository {

    var sessions: Flow<StoredSession> = emptyFlow()

    override fun watchSession(): Flow<StoredSession> = sessions
}
