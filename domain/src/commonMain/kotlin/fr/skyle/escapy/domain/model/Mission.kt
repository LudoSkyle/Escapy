package fr.skyle.escapy.domain.model

/**
 * A mission of a game. A player sees it once unlocked, without its answers nor its trigger; the
 * game master sees every mission, with both.
 */
data class Mission(
    val id: String,
    val title: String,
    val statement: String,
    val type: MissionType,
    // The number of digits of a code, null for a text
    val codeLength: Int?,
    val isFinal: Boolean,
    val state: MissionState,
    // Who solved it, once solved: a player, or the game master in their place
    val solver: MissionSolver?,
    // For the game master only: empty for a player
    val answers: List<String>,
    // For the game master only: null for a player
    val trigger: MissionTrigger?,
)

enum class MissionType {
    // Digits of a fixed length, on a combination lock
    CODE,

    // A word or a sentence, compared without case, accents nor extra spaces
    TEXT,
}

enum class MissionState {
    // The players do not see it yet
    LOCKED,
    ACTIVE,
    SOLVED,
}

data class MissionSolver(
    val userId: String,
    val username: String,
    val isCurrentUser: Boolean,
)

/** What makes a mission visible to the players. */
sealed interface MissionTrigger {
    // When the game master unlocks it
    data object Manual : MissionTrigger

    // When the game starts
    data object OnStart : MissionTrigger

    // When the mission [missionId] is solved
    data class AfterMission(val missionId: String) : MissionTrigger
}

/** A mission as the game master writes it: [missionId] is null for a new one. */
data class MissionDraft(
    val missionId: String?,
    val title: String,
    val statement: String,
    val type: MissionType,
    val answers: List<String>,
    val trigger: MissionTrigger,
    val isFinal: Boolean,
)
