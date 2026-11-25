package fr.skyle.escapy.domain.usecase.account

/**
 * Signs in again with the current password, then sends a link to the new email: the address changes
 * once it is opened. Throws InvalidCredentialsException when the current password is wrong.
 */
interface ChangeEmailUseCase {
    suspend operator fun invoke(currentPassword: String, newEmail: String)
}
