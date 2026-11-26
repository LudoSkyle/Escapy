package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The data of createGame: no maximum (null) lets in as many players as the server allows. */
@Serializable
internal data class CreateGameRequest(
    val title: String,
    val durationMinutes: Int,
    val maxPlayers: Int?,
)
