package fr.skyle.escapy.domain.usecase.auth

/**
 * Creates an account with an email and a password, and signs in with it. Throws
 * WeakPasswordException before any call when the password breaks the policy.
 */
interface SignUpWithEmailUseCase {
    suspend operator fun invoke(email: String, password: String)
}
