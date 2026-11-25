package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/** The profile of a user, then each change; null while it does not exist yet. */
interface WatchProfileUseCase {
    operator fun invoke(userId: String): Flow<Profile?>
}
