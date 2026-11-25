package fr.skyle.escapy.data.repository.profile

import fr.skyle.escapy.data.document.ProfileDocument
import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.functionCall
import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.firebase.FirebaseServices
import fr.skyle.escapy.data.function.EmptyRequest
import fr.skyle.escapy.data.function.ProfileResponse
import fr.skyle.escapy.data.function.UpdateProfileRequest
import fr.skyle.escapy.data.model.StoredProfile
import fr.skyle.escapy.data.repository.profile.api.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class ProfileRepositoryImpl(
    private val firebaseProvider: FirebaseProvider,
) : ProfileRepository {

    private fun services(): FirebaseServices =
        firebaseProvider.services() ?: throw FirebaseUnavailableException()

    override fun watchProfile(userId: String): Flow<StoredProfile?> =
        services().firestore.collection(USERS).document(userId).snapshots
            .map { snapshot ->
                if (snapshot.exists) snapshot.data<ProfileDocument>().toStoredProfile() else null
            }
            .distinctUntilChanged()

    override suspend fun ensureProfile(): StoredProfile {
        val callable = services().functions.httpsCallable(ENSURE_PROFILE)
        val response = functionCall { callable.invoke(EmptyRequest).data<ProfileResponse>() }
        return response.toStoredProfile()
    }

    override suspend fun updateProfile(username: String?, avatarId: Int?): StoredProfile {
        val callable = services().functions.httpsCallable(UPDATE_PROFILE)
        val request = UpdateProfileRequest(username = username, avatarId = avatarId)
        val response = functionCall {
            // A field left null is not sent: the function changes only what it receives
            callable.invoke(request) { encodeDefaults = false }.data<ProfileResponse>()
        }
        return response.toStoredProfile()
    }

    private companion object {
        const val USERS = "users"
        const val ENSURE_PROFILE = "ensureProfile"
        const val UPDATE_PROFILE = "updateProfile"
    }
}

private fun ProfileDocument.toStoredProfile(): StoredProfile =
    StoredProfile(
        username = username,
        avatarId = avatarId,
    )

private fun ProfileResponse.toStoredProfile(): StoredProfile =
    StoredProfile(
        username = username,
        avatarId = avatarId,
    )
