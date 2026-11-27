package fr.skyle.escapy.data.document

import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

/**
 * games/{gameId}/messages/{messageId}, as read: the time of the server, or its estimate while the
 * message waits to be acknowledged (see docs/KMP/01_MODULE_DATA.md).
 */
@Serializable
internal data class MessageDocument(
    val authorId: String = "",
    val author: MemberDocument = MemberDocument(),
    val text: String = "",
    val sentAt: Timestamp? = null,
)

/** A message as the app writes it: the rules check each field, and the time is the server's. */
@Serializable
internal data class NewMessageDocument(
    val authorId: String,
    val author: MemberDocument,
    val text: String,
    val sentAt: BaseTimestamp,
)
