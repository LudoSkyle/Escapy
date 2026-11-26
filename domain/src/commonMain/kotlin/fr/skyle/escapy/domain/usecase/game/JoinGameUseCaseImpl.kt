package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.domain.mapper.withDomainExceptions
import fr.skyle.escapy.domain.validation.GameRules

internal class JoinGameUseCaseImpl(
    private val gameRepository: GameRepository,
) : JoinGameUseCase {

    override suspend fun invoke(code: String): String =
        withDomainExceptions { gameRepository.joinGame(code = GameRules.cleanJoinCode(code)) }
}
