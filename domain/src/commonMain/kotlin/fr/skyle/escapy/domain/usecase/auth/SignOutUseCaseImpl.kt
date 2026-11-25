package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class SignOutUseCaseImpl(
    private val authRepository: AuthRepository,
) : SignOutUseCase {

    override suspend fun invoke() =
        withDomainExceptions { authRepository.signOut() }
}
