package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The answer of getServerTime: the time of the server, in milliseconds since the epoch. */
@Serializable
internal data class ServerTimeResponse(
    val nowMs: Long,
)
