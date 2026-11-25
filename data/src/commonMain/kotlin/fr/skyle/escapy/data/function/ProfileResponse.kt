package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The answer of ensureProfile and updateProfile. */
@Serializable
internal data class ProfileResponse(
    val username: String,
    val avatarId: Int,
)
