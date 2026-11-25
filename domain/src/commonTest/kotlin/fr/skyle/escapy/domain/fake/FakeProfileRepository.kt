package fr.skyle.escapy.domain.fake

import fr.skyle.escapy.data.model.StoredProfile
import fr.skyle.escapy.data.repository.profile.api.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeProfileRepository : ProfileRepository {

    var profiles: Flow<StoredProfile?> = emptyFlow()

    var ensuredProfile = StoredProfile(username = "Player_ABC234", avatarId = 7)

    var ensureFailure: Exception? = null

    override fun watchProfile(userId: String): Flow<StoredProfile?> = profiles

    override suspend fun ensureProfile(): StoredProfile {
        ensureFailure?.let { exception -> throw exception }
        return ensuredProfile
    }

    var updateFailure: Exception? = null

    val updates = mutableListOf<Pair<String?, Int?>>()

    override suspend fun updateProfile(username: String?, avatarId: Int?): StoredProfile {
        updates += username to avatarId
        updateFailure?.let { exception -> throw exception }
        return StoredProfile(
            username = username ?: ensuredProfile.username,
            avatarId = avatarId ?: ensuredProfile.avatarId,
        )
    }
}
