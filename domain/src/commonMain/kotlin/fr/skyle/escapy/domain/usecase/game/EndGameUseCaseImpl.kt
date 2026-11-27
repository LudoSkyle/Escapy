package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class EndGameUseCaseImpl(
    private val gameRepository: GameRepository,
) : EndGameUseCase {

    override suspend fun invoke(gameId: String) {
        withDomainExceptions { gameRepository.endGame(gameId = gameId) }
    }
}
