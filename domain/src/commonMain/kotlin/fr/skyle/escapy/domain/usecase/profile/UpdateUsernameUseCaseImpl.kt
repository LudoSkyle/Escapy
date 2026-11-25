package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.data.repository.profile.api.ProfileRepository
import fr.skyle.escapy.domain.mapper.toProfile
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.model.Profile

internal class UpdateUsernameUseCaseImpl(
    private val profileRepository: ProfileRepository,
) : UpdateUsernameUseCase {

    override suspend fun invoke(username: String): Profile =
        withDomainExceptions { profileRepository.updateProfile(username = username).toProfile() }
}
