package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import fr.skyle.escapy.domain.mapper.toDomainException
import fr.skyle.escapy.domain.mapper.toGame
import fr.skyle.escapy.domain.model.Game
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
internal class WatchMyGamesUseCaseImpl(
    private val sessionRepository: SessionRepository,
    private val gameRepository: GameRepository,
) : WatchMyGamesUseCase {

    override fun invoke(): Flow<List<Game>> =
        sessionRepository.watchSession()
            .map { session -> (session as? StoredSession.SignedIn)?.userId }
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                if (userId == null) {
                    flowOf(emptyList())
                } else {
                    gameRepository.watchGamesOf(userId = userId)
                        .map { games -> games.map { game -> game.toGame(currentUserId = userId) } }
                }
            }
            .catch { exception -> throw exception.toDomainException() }
}
