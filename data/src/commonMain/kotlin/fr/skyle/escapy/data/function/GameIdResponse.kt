package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The answer of createGame and joinGame: the game to open. */
@Serializable
internal data class GameIdResponse(
    val gameId: String,
)
