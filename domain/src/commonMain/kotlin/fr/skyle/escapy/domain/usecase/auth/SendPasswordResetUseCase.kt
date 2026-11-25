package fr.skyle.escapy.domain.usecase.auth

/** Sends the email to reset a password, whether the address has an account or not. */
interface SendPasswordResetUseCase {
    suspend operator fun invoke(email: String)
}
