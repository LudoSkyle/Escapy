package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.data.repository.mission.api.MissionRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class SubmitAnswerUseCaseImpl(
    private val missionRepository: MissionRepository,
) : SubmitAnswerUseCase {

    override suspend fun invoke(gameId: String, missionId: String, answer: String): Boolean =
        withDomainExceptions { missionRepository.submitAnswer(gameId = gameId, missionId = missionId, answer = answer.trim()) }
}
