package fr.skyle.escapy.domain.usecase.auth

/**
 * Signs in with the ID token Google gave the app, creating the account the first time. Throws
 * AccountExistsWithPasswordException when the email already has an account with a password.
 */
interface SignInWithGoogleUseCase {
    suspend operator fun invoke(idToken: String)
}
