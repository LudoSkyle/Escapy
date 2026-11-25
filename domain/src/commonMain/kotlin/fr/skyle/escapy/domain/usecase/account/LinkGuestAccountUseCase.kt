package fr.skyle.escapy.domain.usecase.account

/**
 * Gives the guest account an email and a password, keeping everything in it, and sends the email to
 * verify the address. Throws WeakPasswordException before any call when the password breaks the
 * policy, EmailAlreadyInUseException when another account uses the email.
 */
interface LinkGuestAccountUseCase {
    suspend operator fun invoke(email: String, password: String)
}
