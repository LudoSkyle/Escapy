package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class CancelGameUseCaseImpl(
    private val gameRepository: GameRepository,
) : CancelGameUseCase {

    override suspend fun invoke(gameId: String) {
        withDomainExceptions { gameRepository.cancelGame(gameId = gameId) }
    }
}
