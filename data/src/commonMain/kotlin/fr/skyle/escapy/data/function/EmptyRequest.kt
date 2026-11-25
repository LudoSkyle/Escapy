package fr.skyle.escapy.data.function

import kotlinx.serialization.Serializable

/** The data of a callable that needs none: an empty object, which the functions expect. */
@Serializable
internal data object EmptyRequest
