package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.repository.account.api.AccountRepository
import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class DeleteAccountUseCaseImpl(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
) : DeleteAccountUseCase {

    override suspend fun invoke(currentPassword: String?) =
        withDomainExceptions {
            if (currentPassword != null) {
                authRepository.reauthenticate(password = currentPassword)
            }
            accountRepository.deleteAccount()
        }
}
