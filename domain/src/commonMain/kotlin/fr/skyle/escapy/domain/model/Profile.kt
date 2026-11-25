package fr.skyle.escapy.domain.model

/** What the other players see of a user: a name and one of the avatars of the app. */
data class Profile(
    val username: String,
    val avatarId: Int,
)
