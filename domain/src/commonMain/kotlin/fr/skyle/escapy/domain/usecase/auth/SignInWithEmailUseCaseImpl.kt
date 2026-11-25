package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class SignInWithEmailUseCaseImpl(
    private val authRepository: AuthRepository,
) : SignInWithEmailUseCase {

    override suspend fun invoke(email: String, password: String) =
        withDomainExceptions { authRepository.signInWithEmail(email = email, password = password) }
}
