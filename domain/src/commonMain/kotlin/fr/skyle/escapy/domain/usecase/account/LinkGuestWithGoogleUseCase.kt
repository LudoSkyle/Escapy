package fr.skyle.escapy.domain.usecase.account

import fr.skyle.escapy.domain.model.GoogleTokens

/**
 * Turns the guest account into an account that signs in with Google, keeping everything in it.
 * Throws GoogleAccountAlreadyInUseException when this Google account already has an account.
 */
interface LinkGuestWithGoogleUseCase {
    suspend operator fun invoke(tokens: GoogleTokens)
}
