package fr.skyle.escapy.data.repository.game.api

import fr.skyle.escapy.data.model.StoredGame
import fr.skyle.escapy.data.model.StoredGameEntry
import fr.skyle.escapy.data.model.StoredMember
import kotlinx.coroutines.flow.Flow

interface GameRepository {

    /** The games [userId] runs or plays, from their own list, the latest first, then each change. */
    fun watchGamesOf(userId: String): Flow<List<StoredGameEntry>>

    /**
     * A game, then each change: null once it is deleted. Throws AccessDeniedException once the user
     * is no longer a member, such as a player excluded by the game master.
     */
    fun watchGame(gameId: String): Flow<StoredGame?>

    /** The players of a game, the game master apart, in the order they joined, then each change. */
    fun watchPlayers(gameId: String): Flow<List<StoredMember>>

    /** Creates a waiting game run by the signed-in user, and returns its id. */
    suspend fun createGame(title: String, durationMinutes: Int, maxPlayers: Int?): String

    /** Lets the signed-in user into the game of [code], and returns its id. */
    suspend fun joinGame(code: String): String

    /** Takes the signed-in user out of a game they play. */
    suspend fun leaveGame(gameId: String)

    /** Excludes a player from a game the signed-in user runs. */
    suspend fun kickPlayer(gameId: String, playerId: String)

    /** Deletes a game the signed-in user runs, for everyone. */
    suspend fun cancelGame(gameId: String)

    /** Starts the timer of a waiting game the signed-in user runs. */
    suspend fun startGame(gameId: String)

    /** Pauses the timer of a running game the signed-in user runs. */
    suspend fun pauseGame(gameId: String)

    /** Starts again the timer of a paused game the signed-in user runs. */
    suspend fun resumeGame(gameId: String)

    /** Adds [minutes] to a game the signed-in user runs, which had [addedMinutes] added so far. */
    suspend fun addTime(gameId: String, minutes: Int, addedMinutes: Int)

    /** Ends for good a started game the signed-in user runs. */
    suspend fun endGame(gameId: String)

    /** Asks the server to record that the time of a game is up, which it checks by its own clock. */
    suspend fun expireGame(gameId: String)
}
