package fr.skyle.escapy.domain.usecase.game

/**
 * Lets the user into the game of a code, typed as it comes, and returns its id. Throws
 * InvalidJoinCodeException, GameNotFoundException, GameFullException, GameNotJoinableException,
 * ExcludedFromGameException or TooManyAttemptsException when the server refuses.
 */
interface JoinGameUseCase {
    suspend operator fun invoke(code: String): String
}
