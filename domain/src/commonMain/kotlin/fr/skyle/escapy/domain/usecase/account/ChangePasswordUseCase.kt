package fr.skyle.escapy.domain.usecase.account

/**
 * Signs in again with the current password, then sets the new one. Throws WeakPasswordException
 * before any call when the new password breaks the policy, InvalidCredentialsException when the
 * current one is wrong.
 */
interface ChangePasswordUseCase {
    suspend operator fun invoke(currentPassword: String, newPassword: String)
}
