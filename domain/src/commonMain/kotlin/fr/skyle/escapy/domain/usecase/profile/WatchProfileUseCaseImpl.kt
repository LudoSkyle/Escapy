package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.data.repository.profile.api.ProfileRepository
import fr.skyle.escapy.domain.mapper.toDomainException
import fr.skyle.escapy.domain.mapper.toProfile
import fr.skyle.escapy.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal class WatchProfileUseCaseImpl(
    private val profileRepository: ProfileRepository,
) : WatchProfileUseCase {

    override fun invoke(userId: String): Flow<Profile?> =
        profileRepository.watchProfile(userId = userId)
            .map { storedProfile -> storedProfile?.toProfile() }
            .catch { exception -> throw exception.toDomainException() }
}
