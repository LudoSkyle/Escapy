package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.data.repository.profile.api.ProfileRepository
import fr.skyle.escapy.domain.mapper.toProfile
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.model.Profile

internal class EnsureProfileUseCaseImpl(
    private val profileRepository: ProfileRepository,
) : EnsureProfileUseCase {

    override suspend fun invoke(): Profile =
        withDomainExceptions { profileRepository.ensureProfile().toProfile() }
}
