package fr.skyle.escapy.fake

import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class FakeWatchSessionUseCase : WatchSessionUseCase {

    val sessions = MutableSharedFlow<Session>()

    // When set, the session fails to be read instead
    var failure: Exception? = null

    override fun invoke(): Flow<Session> {
        val exception = failure ?: return sessions
        return flow { throw exception }
    }
}
