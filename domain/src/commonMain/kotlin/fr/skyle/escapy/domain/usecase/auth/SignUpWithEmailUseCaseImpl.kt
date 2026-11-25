package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.exception.WeakPasswordException
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.validation.PasswordPolicy

internal class SignUpWithEmailUseCaseImpl(
    private val authRepository: AuthRepository,
) : SignUpWithEmailUseCase {

    override suspend fun invoke(email: String, password: String) {
        val missingRules = PasswordPolicy.missingRules(password)
        if (missingRules.isNotEmpty()) {
            throw WeakPasswordException(missingRules = missingRules)
        }
        withDomainExceptions { authRepository.signUpWithEmail(email = email, password = password) }
    }
}
