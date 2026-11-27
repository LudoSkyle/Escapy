package fr.skyle.escapy.domain.model

import kotlin.time.Instant

/** A message of the chat of a game, with its author as they were when they wrote it. */
data class ChatMessage(
    val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarId: Int,
    val text: String,
    // The time of the server, estimated while the server has not acknowledged the message yet
    val sentAt: Instant?,
    val isMine: Boolean,
    // Shown at once, not yet acknowledged by the server
    val isPending: Boolean,
)
