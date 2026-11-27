package fr.skyle.escapy.data.model

/** A mission of a game: as the players see it, or, for the game master, with its answers and trigger. */
data class StoredMission(
    val id: String,
    val title: String,
    val statement: String,
    val type: String,
    val codeLength: Int?,
    val isFinal: Boolean,
    val state: String,
    val solverId: String?,
    val solverUsername: String?,
    // For the game master only: empty for a player
    val answers: List<String>,
    val triggerType: String?,
    val triggerMissionId: String?,
)
