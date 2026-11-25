package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The data of updateProfile: a field left null is not sent, and stays as it is. */
@Serializable
internal data class UpdateProfileRequest(
    val username: String? = null,
    val avatarId: Int? = null,
)
