package fr.skyle.escapy.data.model

/** A message of the chat of a game, with its author as they were when they wrote it. */
data class StoredMessage(
    val id: String,
    val authorId: String,
    val authorUsername: String,
    val authorAvatarId: Int,
    val text: String,
    // The time of the server, estimated while the message waits to be acknowledged
    val sentAtMs: Long?,
    // Shown at once, not yet acknowledged by the server
    val isPending: Boolean,
)
