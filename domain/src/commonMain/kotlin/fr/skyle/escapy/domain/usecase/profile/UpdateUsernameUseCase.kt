package fr.skyle.escapy.domain.usecase.profile

import fr.skyle.escapy.domain.model.Profile

/** Changes the username. Throws InvalidUsernameException when the server refuses it. */
interface UpdateUsernameUseCase {
    suspend operator fun invoke(username: String): Profile
}
