package fr.skyle.escapy.domain.usecase.account

/**
 * Deletes the account and all its data, then signs out. A member gives their [currentPassword] to
 * sign in again first; a guest, who has none, gives null. Also what signing out does for a guest:
 * an account nobody can sign in to again is not left on the server.
 */
interface DeleteAccountUseCase {
    suspend operator fun invoke(currentPassword: String?)
}
