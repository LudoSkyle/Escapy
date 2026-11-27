package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class ExpireGameUseCaseImpl(
    private val gameRepository: GameRepository,
) : ExpireGameUseCase {

    override suspend fun invoke(gameId: String) {
        withDomainExceptions { gameRepository.expireGame(gameId = gameId) }
    }
}
