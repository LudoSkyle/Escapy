package fr.skyle.escapy.domain.usecase.game

/** Starts the timer of a waiting game the user runs, from its duration. */
interface StartGameUseCase {
    suspend operator fun invoke(gameId: String)
}
