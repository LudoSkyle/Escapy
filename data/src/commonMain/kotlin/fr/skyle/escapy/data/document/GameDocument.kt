package fr.skyle.escapy.data.document

import dev.gitlive.firebase.firestore.Timestamp
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
    // The timer: endsAt while the game runs, remainingMs while it is paused or once it is over
    val endsAt: Timestamp? = null,
    val remainingMs: Long? = null,
    val addedMinutes: Int = 0,
    // Once the game is over
    val result: String? = null,
    val playedMs: Long? = null,
)

/** A member of a game as the others see them: copied from their profile. */
@Serializable
internal data class MemberDocument(
    val username: String = "",
    val avatarId: Int = 1,
)
