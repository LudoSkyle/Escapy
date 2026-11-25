package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.exception.WeakPasswordException
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.validation.PasswordPolicy

internal class LinkGuestAccountUseCaseImpl(
    private val authRepository: AuthRepository,
) : LinkGuestAccountUseCase {

    override suspend fun invoke(email: String, password: String) {
        val missingRules = PasswordPolicy.missingRules(password)
        if (missingRules.isNotEmpty()) {
            throw WeakPasswordException(missingRules = missingRules)
        }
        withDomainExceptions { authRepository.linkGuestWithEmail(email = email, password = password) }
    }
}
