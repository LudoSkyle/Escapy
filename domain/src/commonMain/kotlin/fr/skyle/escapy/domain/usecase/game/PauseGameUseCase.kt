package fr.skyle.escapy.domain.usecase.game

/**
 * Pauses the timer of a running game the user runs. Throws GameNotStartedException before it
 * starts.
 */
interface PauseGameUseCase {
    suspend operator fun invoke(gameId: String)
}
