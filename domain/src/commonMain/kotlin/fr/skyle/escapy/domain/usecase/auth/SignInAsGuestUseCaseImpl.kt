package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class SignInAsGuestUseCaseImpl(
    private val authRepository: AuthRepository,
) : SignInAsGuestUseCase {

    override suspend fun invoke() =
        withDomainExceptions { authRepository.signInAsGuest() }
}
