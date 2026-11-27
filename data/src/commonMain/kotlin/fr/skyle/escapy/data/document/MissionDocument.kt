package fr.skyle.escapy.data.document

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

/** games/{gameId}/missions/{missionId}: a mission as the players see it, never an answer. */
@Serializable
internal data class MissionDocument(
    val title: String = "",
    val statement: String = "",
    val type: String = "",
    val codeLength: Int? = null,
    val isFinal: Boolean = false,
    val state: String = "",
    val createdAt: Timestamp? = null,
    val solvedBy: SolverDocument? = null,
)

/** games/{gameId}/masterMissions/{missionId}: a mission as the game master wrote it, answers included. */
@Serializable
internal data class MasterMissionDocument(
    val title: String = "",
    val statement: String = "",
    val type: String = "",
    val answers: List<String> = emptyList(),
    val codeLength: Int? = null,
    val trigger: TriggerDocument = TriggerDocument(),
    val isFinal: Boolean = false,
    val state: String = "",
    val createdAt: Timestamp? = null,
    val solvedBy: SolverDocument? = null,
)

/** Who solved a mission: a player, or the game master in their place. */
@Serializable
internal data class SolverDocument(
    val userId: String = "",
    val username: String = "",
)

/** What makes a mission visible: MANUAL, ON_START, or AFTER_MISSION with the mission it follows. */
@Serializable
internal data class TriggerDocument(
    val type: String = "MANUAL",
    val missionId: String? = null,
)
