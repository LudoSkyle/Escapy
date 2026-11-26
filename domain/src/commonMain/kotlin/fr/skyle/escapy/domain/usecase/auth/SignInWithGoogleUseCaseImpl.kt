package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.model.GoogleTokens

internal class SignInWithGoogleUseCaseImpl(
    private val authRepository: AuthRepository,
) : SignInWithGoogleUseCase {

    override suspend fun invoke(tokens: GoogleTokens) =
        withDomainExceptions { authRepository.signInWithGoogle(idToken = tokens.idToken, accessToken = tokens.accessToken) }
}
