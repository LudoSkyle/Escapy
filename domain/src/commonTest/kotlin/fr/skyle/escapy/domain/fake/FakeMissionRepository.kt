package fr.skyle.escapy.domain.fake

import fr.skyle.escapy.data.model.StoredMission
import fr.skyle.escapy.data.repository.mission.api.MissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeMissionRepository : MissionRepository {

    var missions: Flow<List<StoredMission>> = emptyFlow()
    var masterMissions: Flow<List<StoredMission>> = emptyFlow()

    /** What submitAnswer answers. */
    var isCorrect = false

    /** Thrown by the next calls when set. */
    var failure: Exception? = null

    val calls = mutableListOf<String>()

    override fun watchMissions(gameId: String): Flow<List<StoredMission>> {
        calls += "watchMissions"
        return missions
    }

    override fun watchMasterMissions(gameId: String): Flow<List<StoredMission>> {
        calls += "watchMasterMissions"
        return masterMissions
    }

    override suspend fun saveMission(
        gameId: String,
        missionId: String,
        title: String,
        statement: String,
        type: String,
        answers: List<String>,
        triggerType: String,
        triggerMissionId: String?,
        isFinal: Boolean,
    ) = record("save:$missionId:$title:$type:${answers.joinToString("|")}:$triggerType:$triggerMissionId:$isFinal")

    override suspend fun deleteMission(gameId: String, missionId: String) = record("delete:$missionId")

    override suspend fun unlockMission(gameId: String, missionId: String) = record("unlock:$missionId")

    override suspend fun validateMission(gameId: String, missionId: String) = record("validate:$missionId")

    override suspend fun submitAnswer(gameId: String, missionId: String, answer: String): Boolean {
        record("submit:$missionId:$answer")
        return isCorrect
    }

    private fun record(call: String) {
        calls += call
        failure?.let { exception -> throw exception }
    }
}

fun storedMission(
    id: String,
    state: String = "ACTIVE",
    solverId: String? = null,
    triggerType: String? = null,
    triggerMissionId: String? = null,
): StoredMission =
    StoredMission(
        id = id,
        title = "Mission $id",
        statement = "Trouvez le code.",
        type = "CODE",
        codeLength = 4,
        isFinal = false,
        state = state,
        solverId = solverId,
        solverUsername = solverId?.let { userId -> "Name of $userId" },
        answers = if (triggerType == null) emptyList() else listOf("0427"),
        triggerType = triggerType,
        triggerMissionId = triggerMissionId,
    )
