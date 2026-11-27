package fr.skyle.escapy.domain.usecase.game

/** Ends for good a started game the user runs. Throws GameNotStartedException before it starts. */
interface EndGameUseCase {
    suspend operator fun invoke(gameId: String)
}
