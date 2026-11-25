package fr.skyle.escapy.data.document

import kotlinx.serialization.Serializable

/** users/{uid}, as the Cloud Functions write it (see docs/Backend/README.md). */
@Serializable
internal data class ProfileDocument(
    val username: String = "",
    val avatarId: Int = 1,
)
