package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.data.repository.mission.api.MissionRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class DeleteMissionUseCaseImpl(
    private val missionRepository: MissionRepository,
) : DeleteMissionUseCase {

    override suspend fun invoke(gameId: String, missionId: String) {
        withDomainExceptions { missionRepository.deleteMission(gameId = gameId, missionId = missionId) }
    }
}
