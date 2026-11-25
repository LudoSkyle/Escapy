package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class SignInWithGoogleUseCaseImpl(
    private val authRepository: AuthRepository,
) : SignInWithGoogleUseCase {

    override suspend fun invoke(idToken: String) =
        withDomainExceptions { authRepository.signInWithGoogle(idToken = idToken) }
}
