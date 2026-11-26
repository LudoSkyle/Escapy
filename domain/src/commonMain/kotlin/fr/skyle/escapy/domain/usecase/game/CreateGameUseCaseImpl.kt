package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class CreateGameUseCaseImpl(
    private val gameRepository: GameRepository,
) : CreateGameUseCase {

    override suspend fun invoke(title: String, durationMinutes: Int, maxPlayers: Int?): String =
        withDomainExceptions {
            gameRepository.createGame(title = title, durationMinutes = durationMinutes, maxPlayers = maxPlayers)
        }
}
