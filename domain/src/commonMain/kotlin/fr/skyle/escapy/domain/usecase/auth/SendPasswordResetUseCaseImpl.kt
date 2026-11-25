package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class SendPasswordResetUseCaseImpl(
    private val authRepository: AuthRepository,
) : SendPasswordResetUseCase {

    override suspend fun invoke(email: String) =
        withDomainExceptions { authRepository.sendPasswordReset(email = email) }
}
