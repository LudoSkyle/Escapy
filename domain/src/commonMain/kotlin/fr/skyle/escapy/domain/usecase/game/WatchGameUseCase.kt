package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.domain.model.GameState
import kotlinx.coroutines.flow.Flow

/**
 * A game with its players, then each change, until it is deleted or the user excluded from it: the
 * flow ends with that state.
 */
interface WatchGameUseCase {
    operator fun invoke(gameId: String): Flow<GameState>
}
