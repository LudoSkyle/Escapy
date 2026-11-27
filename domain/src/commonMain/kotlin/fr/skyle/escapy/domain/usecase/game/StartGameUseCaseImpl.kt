package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class StartGameUseCaseImpl(
    private val gameRepository: GameRepository,
) : StartGameUseCase {

    override suspend fun invoke(gameId: String) {
        withDomainExceptions { gameRepository.startGame(gameId = gameId) }
    }
}
