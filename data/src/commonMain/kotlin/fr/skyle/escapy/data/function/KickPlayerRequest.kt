package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The data of kickPlayer. */
@Serializable
internal data class KickPlayerRequest(
    val gameId: String,
    val playerId: String,
)
