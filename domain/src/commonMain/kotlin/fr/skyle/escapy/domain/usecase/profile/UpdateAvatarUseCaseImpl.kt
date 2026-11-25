package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.data.repository.profile.api.ProfileRepository
import fr.skyle.escapy.domain.mapper.toProfile
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.model.Profile

internal class UpdateAvatarUseCaseImpl(
    private val profileRepository: ProfileRepository,
) : UpdateAvatarUseCase {

    override suspend fun invoke(avatarId: Int): Profile =
        withDomainExceptions { profileRepository.updateProfile(avatarId = avatarId).toProfile() }
}
