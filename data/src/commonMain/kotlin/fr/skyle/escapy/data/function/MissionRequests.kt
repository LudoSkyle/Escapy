package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The data of saveMission: the mission as the game master writes it, its id chosen by the app. */
@Serializable
internal data class SaveMissionRequest(
    val gameId: String,
    val missionId: String,
    val title: String,
    val statement: String,
    val type: String,
    val answers: List<String>,
    val trigger: TriggerRequest,
    val isFinal: Boolean,
)

@Serializable
internal data class TriggerRequest(
    val type: String,
    val missionId: String? = null,
)

/** The data of the callables about one mission: deleteMission, unlockMission, validateMission. */
@Serializable
internal data class MissionRequest(
    val gameId: String,
    val missionId: String,
)

@Serializable
internal data class SubmitAnswerRequest(
    val gameId: String,
    val missionId: String,
    val answer: String,
)

/** The answer of submitAnswer: right or wrong, nothing more. */
@Serializable
internal data class SubmitAnswerResponse(
    val correct: Boolean,
)
