package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.domain.model.Mission
import kotlinx.coroutines.flow.Flow

/**
 * The missions of a game, in the order they were created, then each change: for its game master
 * ([isMaster]), every mission with its answers and trigger; for a player, the unlocked ones only.
 * Ends once the user can no longer read the game: the game itself tells why.
 */
interface WatchMissionsUseCase {
    operator fun invoke(gameId: String, isMaster: Boolean): Flow<List<Mission>>
}
