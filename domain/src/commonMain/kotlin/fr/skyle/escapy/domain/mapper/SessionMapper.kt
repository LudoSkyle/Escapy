package fr.skyle.escapy.domain.mapper

import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.domain.model.Session

internal fun StoredSession.toSession(): Session =
    when (this) {
        StoredSession.SignedOut -> Session.SignedOut
        is StoredSession.SignedIn -> Session.SignedIn(userId = userId, isGuest = isAnonymous)
    }
