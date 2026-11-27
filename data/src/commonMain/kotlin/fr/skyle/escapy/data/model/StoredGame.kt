package fr.skyle.escapy.data.model

/** A game as Firestore keeps it: readable by its members only. */
data class StoredGame(
    val id: String,
    val title: String,
    val durationMinutes: Int,
    // Null when the game master set none: the server lets in up to its own limit
    val maxPlayers: Int?,
    val status: String,
    val masterId: String,
    val master: StoredMember,
    val playerCount: Int,
    val joinCode: String,
    // When the timer of a running game reaches zero, by the clock of the server
    val endsAtMs: Long?,
    // The time left to a paused game, or to a finished one
    val remainingMs: Long?,
    val addedMinutes: Int,
    val result: String?,
    val playedMs: Long?,
)

/** A member of a game: their id, and the name and avatar copied from their profile. */
data class StoredMember(
    val userId: String,
    val username: String,
    val avatarId: Int,
)
