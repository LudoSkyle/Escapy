package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.data.repository.mission.api.MissionRepository
import fr.skyle.escapy.domain.exception.InvalidMissionException
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.model.MissionDraft
import fr.skyle.escapy.domain.model.MissionTrigger
import fr.skyle.escapy.domain.validation.MissionRules
import kotlin.random.Random

internal class SaveMissionUseCaseImpl(
    private val missionRepository: MissionRepository,
) : SaveMissionUseCase {

    override suspend fun invoke(gameId: String, draft: MissionDraft): String {
        if (!MissionRules.isValid(draft)) throw InvalidMissionException()
        val missionId = draft.missionId ?: newMissionId()
        withDomainExceptions {
            missionRepository.saveMission(
                gameId = gameId,
                missionId = missionId,
                title = draft.title.trim(),
                statement = draft.statement.trim(),
                type = draft.type.name,
                answers = MissionRules.cleanAnswers(draft.answers),
                triggerType = when (draft.trigger) {
                    MissionTrigger.Manual -> "MANUAL"
                    MissionTrigger.OnStart -> "ON_START"
                    is MissionTrigger.AfterMission -> "AFTER_MISSION"
                },
                triggerMissionId = (draft.trigger as? MissionTrigger.AfterMission)?.missionId,
                isFinal = draft.isFinal,
            )
        }
        return missionId
    }

    private fun newMissionId(): String =
        (1..ID_LENGTH).map { ID_ALPHABET[Random.nextInt(ID_ALPHABET.length)] }.joinToString("")

    private companion object {
        const val ID_LENGTH = 20
        const val ID_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }
}
