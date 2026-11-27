package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.data.exception.AccessDeniedException
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.data.repository.mission.api.MissionRepository
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import fr.skyle.escapy.domain.mapper.toDomainException
import fr.skyle.escapy.domain.mapper.toMission
import fr.skyle.escapy.domain.model.Mission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
internal class WatchMissionsUseCaseImpl(
    private val sessionRepository: SessionRepository,
    private val missionRepository: MissionRepository,
) : WatchMissionsUseCase {

    override fun invoke(gameId: String, isMaster: Boolean): Flow<List<Mission>> =
        sessionRepository.watchSession()
            .filterIsInstance<StoredSession.SignedIn>()
            .map { session -> session.userId }
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                // The full missions only for the game master: refused elsewhere, the observation would
                // delay the others on Android (see docs/KMP/01_MODULE_DATA.md)
                val missions = if (isMaster) {
                    missionRepository.watchMasterMissions(gameId = gameId)
                } else {
                    missionRepository.watchMissions(gameId = gameId)
                }
                missions
                    .map { stored -> stored.map { mission -> mission.toMission(currentUserId = userId) } }
                    // A refusal is no error here: the game tells the user why they lost it
                    .catch { exception -> if (exception !is AccessDeniedException) throw exception }
            }
            .catch { exception -> throw exception.toDomainException() }
}
