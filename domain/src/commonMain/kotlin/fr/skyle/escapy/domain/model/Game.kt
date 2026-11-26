package fr.skyle.escapy.domain.model

/** A game the user runs or plays, as its members see it. */
data class Game(
    val id: String,
    val title: String,
    val durationMinutes: Int,
    // Null when the game master set none: the server lets in up to GameRules.PLAYER_LIMIT players
    val maxPlayers: Int?,
    val master: GameMember,
    val playerCount: Int,
    // Shown to the members, who may give it to the next players
    val joinCode: String,
) {
    val isMaster: Boolean
        get() = master.isCurrentUser
}

/** The game master or a player of a game, with the name and avatar copied from their profile. */
data class GameMember(
    val userId: String,
    val username: String,
    val avatarId: Int,
    val isCurrentUser: Boolean,
)
