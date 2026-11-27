package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class ResumeGameUseCaseImpl(
    private val gameRepository: GameRepository,
) : ResumeGameUseCase {

    override suspend fun invoke(gameId: String) {
        withDomainExceptions { gameRepository.resumeGame(gameId = gameId) }
    }
}
