package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.domain.model.GameSummary
import kotlinx.coroutines.flow.Flow

/** The games the signed-in user runs or plays, the latest first, then each change. */
interface WatchMyGamesUseCase {
    operator fun invoke(): Flow<List<GameSummary>>
}
