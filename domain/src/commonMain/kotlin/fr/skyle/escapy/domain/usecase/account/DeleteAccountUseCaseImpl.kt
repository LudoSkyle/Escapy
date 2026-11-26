package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.data.repository.account.api.AccountRepository
import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.model.Reauthentication

internal class DeleteAccountUseCaseImpl(
    private val authRepository: AuthRepository,
    private val accountRepository: AccountRepository,
) : DeleteAccountUseCase {

    override suspend fun invoke(reauthentication: Reauthentication) =
        withDomainExceptions {
            when (reauthentication) {
                Reauthentication.None -> Unit
                is Reauthentication.WithPassword -> authRepository.reauthenticate(password = reauthentication.password)
                is Reauthentication.WithGoogle -> authRepository.reauthenticateWithGoogle(
                    idToken = reauthentication.tokens.idToken,
                    accessToken = reauthentication.tokens.accessToken,
                )
            }
            accountRepository.deleteAccount()
        }
}
