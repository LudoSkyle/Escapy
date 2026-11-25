package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class LinkGuestWithGoogleUseCaseImpl(
    private val authRepository: AuthRepository,
) : LinkGuestWithGoogleUseCase {

    override suspend fun invoke(idToken: String) =
        withDomainExceptions { authRepository.linkGuestWithGoogle(idToken = idToken) }
}
