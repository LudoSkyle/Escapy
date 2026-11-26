package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.model.GoogleTokens

internal class LinkGuestWithGoogleUseCaseImpl(
    private val authRepository: AuthRepository,
) : LinkGuestWithGoogleUseCase {

    override suspend fun invoke(tokens: GoogleTokens) =
        withDomainExceptions { authRepository.linkGuestWithGoogle(idToken = tokens.idToken, accessToken = tokens.accessToken) }
}
