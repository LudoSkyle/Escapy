package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.exception.WeakPasswordException
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.validation.PasswordPolicy

internal class ChangePasswordUseCaseImpl(
    private val authRepository: AuthRepository,
) : ChangePasswordUseCase {

    override suspend fun invoke(currentPassword: String, newPassword: String) {
        val missingRules = PasswordPolicy.missingRules(newPassword)
        if (missingRules.isNotEmpty()) {
            throw WeakPasswordException(missingRules = missingRules)
        }
        withDomainExceptions {
            authRepository.reauthenticate(password = currentPassword)
            authRepository.changePassword(newPassword = newPassword)
        }
    }
}
