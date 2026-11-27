package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.data.repository.mission.api.MissionRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class ValidateMissionUseCaseImpl(
    private val missionRepository: MissionRepository,
) : ValidateMissionUseCase {

    override suspend fun invoke(gameId: String, missionId: String) {
        withDomainExceptions { missionRepository.validateMission(gameId = gameId, missionId = missionId) }
    }
}
