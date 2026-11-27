package fr.skyle.escapy.data.model

/** A game in the list of a user: what their home screen shows of it. */
data class StoredGameEntry(
    val gameId: String,
    val title: String,
    val isMaster: Boolean,
    val masterUsername: String,
    val masterAvatarId: Int,
    val status: String,
)
