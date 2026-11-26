package fr.skyle.escapy.data.document

import kotlinx.serialization.Serializable

/** games/{gameId}, as the Cloud Functions write it (see docs/Backend/README.md). */
@Serializable
internal data class GameDocument(
    val title: String = "",
    val durationMinutes: Int = 0,
    val maxPlayers: Int? = null,
    val status: String = "",
    val masterId: String = "",
    val master: MemberDocument = MemberDocument(),
    val playerCount: Int = 0,
    val joinCode: String = "",
)

/** A member of a game as the others see them: copied from their profile. */
@Serializable
internal data class MemberDocument(
    val username: String = "",
    val avatarId: Int = 1,
)
