package fr.skyle.escapy.domain.usecase.game

/** Starts again the timer of a paused game the user runs, from the time it had left. */
interface ResumeGameUseCase {
    suspend operator fun invoke(gameId: String)
}
