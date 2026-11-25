package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class ChangeEmailUseCaseImpl(
    private val authRepository: AuthRepository,
) : ChangeEmailUseCase {

    override suspend fun invoke(currentPassword: String, newEmail: String) =
        withDomainExceptions {
            authRepository.reauthenticate(password = currentPassword)
            authRepository.changeEmail(newEmail = newEmail)
        }
}
