package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.domain.model.Profile

/** Creates the profile of the signed-in user if it does not exist, and returns it. */
interface EnsureProfileUseCase {
    suspend operator fun invoke(): Profile
}
