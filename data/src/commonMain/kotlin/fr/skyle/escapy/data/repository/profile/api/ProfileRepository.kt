package fr.skyle.escapy.data.repository.profile.api

import fr.skyle.escapy.data.model.StoredProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    /**
     * The profile of [userId], then each change, from the cache of Firestore first: null while it
     * does not exist. Readable by its owner only.
     */
    fun watchProfile(userId: String): Flow<StoredProfile?>

    /** Creates the profile of the signed-in user if it does not exist, and returns it. */
    suspend fun ensureProfile(): StoredProfile
}
