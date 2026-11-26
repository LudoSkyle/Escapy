package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The data of joinGame: the code as typed, which the server cleans. */
@Serializable
internal data class JoinGameRequest(
    val code: String,
)
