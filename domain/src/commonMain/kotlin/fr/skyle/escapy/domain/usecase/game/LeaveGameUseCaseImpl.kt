package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class LeaveGameUseCaseImpl(
    private val gameRepository: GameRepository,
) : LeaveGameUseCase {

    override suspend fun invoke(gameId: String) {
        withDomainExceptions { gameRepository.leaveGame(gameId = gameId) }
    }
}
