package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The data of a callable about one game: leaveGame, cancelGame, and those of the timer. */
@Serializable
internal data class GameRequest(
    val gameId: String,
)
