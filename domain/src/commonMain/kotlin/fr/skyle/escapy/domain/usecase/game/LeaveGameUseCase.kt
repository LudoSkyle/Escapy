package fr.skyle.escapy.domain.usecase.game

/** Takes the user out of a game they play. */
interface LeaveGameUseCase {
    suspend operator fun invoke(gameId: String)
}
