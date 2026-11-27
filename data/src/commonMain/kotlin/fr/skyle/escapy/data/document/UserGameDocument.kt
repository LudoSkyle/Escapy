package fr.skyle.escapy.data.document

import kotlinx.serialization.Serializable

/** users/{uid}/games/{gameId}, the entry of a game in the list of a user (see docs/Backend/README.md). */
@Serializable
internal data class UserGameDocument(
    val title: String = "",
    val isMaster: Boolean = false,
    val master: MemberDocument = MemberDocument(),
    // Written since the timer came: an older entry is of a waiting game
    val status: String = "WAITING",
)
