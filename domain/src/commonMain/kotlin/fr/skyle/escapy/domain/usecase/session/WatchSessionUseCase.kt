package fr.skyle.escapy.domain.usecase.session

import fr.skyle.escapy.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface WatchSessionUseCase {
    operator fun invoke(): Flow<Session>
}
