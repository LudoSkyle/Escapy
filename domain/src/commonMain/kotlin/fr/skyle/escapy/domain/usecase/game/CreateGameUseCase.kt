package fr.skyle.escapy.domain.usecase.game

/**
 * Creates a waiting game run by the user, and returns its id. Throws InvalidGameTitleException or
 * TooManyGamesException when the server refuses it.
 */
interface CreateGameUseCase {
    suspend operator fun invoke(title: String, durationMinutes: Int, maxPlayers: Int?): String
}
