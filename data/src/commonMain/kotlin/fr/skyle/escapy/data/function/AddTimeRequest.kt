package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/**
 * The data of addTime. [addedMinutes] is what the app saw already added: a call sent again after a
 * lost answer finds it changed, and the server adds nothing twice.
 */
@Serializable
internal data class AddTimeRequest(
    val gameId: String,
    val minutes: Int,
    val addedMinutes: Int,
)
