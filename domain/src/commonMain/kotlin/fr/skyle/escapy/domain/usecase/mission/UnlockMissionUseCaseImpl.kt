package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.data.repository.mission.api.MissionRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class UnlockMissionUseCaseImpl(
    private val missionRepository: MissionRepository,
) : UnlockMissionUseCase {

    override suspend fun invoke(gameId: String, missionId: String) {
        withDomainExceptions { missionRepository.unlockMission(gameId = gameId, missionId = missionId) }
    }
}
