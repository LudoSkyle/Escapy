package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.exception.AccessDeniedException
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import fr.skyle.escapy.domain.mapper.toDomainException
import fr.skyle.escapy.domain.mapper.toGame
import fr.skyle.escapy.domain.mapper.toGameMember
import fr.skyle.escapy.domain.model.GameState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile

@OptIn(ExperimentalCoroutinesApi::class)
internal class WatchGameUseCaseImpl(
    private val sessionRepository: SessionRepository,
    private val gameRepository: GameRepository,
) : WatchGameUseCase {

    override fun invoke(gameId: String): Flow<GameState> =
        sessionRepository.watchSession()
            .filterIsInstance<StoredSession.SignedIn>()
            .map { session -> session.userId }
            .distinctUntilChanged()
            .flatMapLatest { userId -> watchGame(gameId = gameId, userId = userId) }
            // A game deleted or lost is the last state: nothing follows it
            .transformWhile { state ->
                emit(state)
                state is GameState.Available
            }

    private fun watchGame(gameId: String, userId: String): Flow<GameState> {
        // The players stop being readable with the game: what tells the reason is the game itself
        val players = gameRepository.watchPlayers(gameId = gameId)
            .catch { exception -> if (exception is AccessDeniedException) emit(emptyList()) else throw exception }
        return combine(gameRepository.watchGame(gameId = gameId), players) { game, storedPlayers ->
            if (game == null) {
                GameState.Deleted
            } else {
                GameState.Available(
                    game = game.toGame(currentUserId = userId),
                    players = storedPlayers.map { player -> player.toGameMember(currentUserId = userId) },
                )
            }
        }
            .catch { exception ->
                if (exception is AccessDeniedException) emit(GameState.Excluded) else throw exception.toDomainException()
            }
    }
}
