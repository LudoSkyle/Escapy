package fr.skyle.escapy.domain.usecase.game

/** Deletes a game the user runs, for everyone. */
interface CancelGameUseCase {
    suspend operator fun invoke(gameId: String)
}
