package fr.skyle.escapy.domain.usecase.auth

import fr.skyle.escapy.domain.model.GoogleTokens

/**
 * Signs in with the tokens Google gave the app, creating the account the first time. Throws
 * AccountExistsWithPasswordException when the email already has an account with a password.
 */
interface SignInWithGoogleUseCase {
    suspend operator fun invoke(tokens: GoogleTokens)
}
