package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class PauseGameUseCaseImpl(
    private val gameRepository: GameRepository,
) : PauseGameUseCase {

    override suspend fun invoke(gameId: String) {
        withDomainExceptions { gameRepository.pauseGame(gameId = gameId) }
    }
}
