package fr.skyle.escapy.domain.mapper

import fr.skyle.escapy.data.model.StoredProfile
import fr.skyle.escapy.domain.model.Profile

internal fun StoredProfile.toProfile(): Profile =
    Profile(
        username = username,
        avatarId = avatarId,
    )
