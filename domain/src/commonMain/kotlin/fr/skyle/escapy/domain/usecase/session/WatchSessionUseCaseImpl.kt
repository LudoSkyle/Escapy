package fr.skyle.escapy.domain.usecase.session

import fr.skyle.escapy.data.repository.session.api.SessionRepository
import fr.skyle.escapy.domain.mapper.toSession
import fr.skyle.escapy.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class WatchSessionUseCaseImpl(
    private val sessionRepository: SessionRepository,
) : WatchSessionUseCase {

    override fun invoke(): Flow<Session> =
        sessionRepository.watchSession().map { storedSession -> storedSession.toSession() }
}
