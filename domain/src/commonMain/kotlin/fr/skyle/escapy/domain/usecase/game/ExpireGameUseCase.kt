package fr.skyle.escapy.domain.usecase.game

/**
 * Asks the server to record that the time of a running game is up: it checks by its own clock, and
 * leaves a game not over yet as it is.
 */
interface ExpireGameUseCase {
    suspend operator fun invoke(gameId: String)
}
