package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions

internal class AddTimeUseCaseImpl(
    private val gameRepository: GameRepository,
) : AddTimeUseCase {

    override suspend fun invoke(gameId: String, minutes: Int, addedMinutes: Int) {
        withDomainExceptions { gameRepository.addTime(gameId = gameId, minutes = minutes, addedMinutes = addedMinutes) }
    }
}
