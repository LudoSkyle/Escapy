package fr.skyle.escapy.data.model

/** The profile of a user: the name and the avatar the other players see. */
data class StoredProfile(
    val username: String,
    val avatarId: Int,
)
