package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class KickPlayerUseCaseImpl(
    private val gameRepository: GameRepository,
) : KickPlayerUseCase {

    override suspend fun invoke(gameId: String, playerId: String) {
        withDomainExceptions { gameRepository.kickPlayer(gameId = gameId, playerId = playerId) }
    }
}
