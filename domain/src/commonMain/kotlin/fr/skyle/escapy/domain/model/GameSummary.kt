package fr.skyle.escapy.domain.model

/** A game in the list of the user, on their home screen: its title, who runs it, and whether it is them. */
data class GameSummary(
    val gameId: String,
    val title: String,
    val isMaster: Boolean,
    val masterUsername: String,
    val masterAvatarId: Int,
    val status: GameStatus,
)
