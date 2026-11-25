package fr.skyle.escapy.domain.usecase.auth

/** Signs in with an email and a password. Throws InvalidCredentialsException when they do not match. */
interface SignInWithEmailUseCase {
    suspend operator fun invoke(email: String, password: String)
}
