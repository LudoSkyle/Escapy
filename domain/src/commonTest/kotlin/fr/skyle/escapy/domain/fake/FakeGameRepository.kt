package fr.skyle.escapy.domain.fake

import fr.skyle.escapy.data.model.StoredGame
import fr.skyle.escapy.data.model.StoredGameEntry
import fr.skyle.escapy.data.model.StoredMember
import fr.skyle.escapy.data.repository.game.api.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeGameRepository : GameRepository {

    var gamesOf: Flow<List<StoredGameEntry>> = emptyFlow()
    var game: Flow<StoredGame?> = emptyFlow()
    var players: Flow<List<StoredMember>> = emptyFlow()

    /** The id createGame and joinGame return. */
    var gameId = "game"

    /** Thrown by the next actions when set. */
    var failure: Exception? = null

    val calls = mutableListOf<String>()

    override fun watchGamesOf(userId: String): Flow<List<StoredGameEntry>> {
        calls += "watchGamesOf:$userId"
        return gamesOf
    }

    override fun watchGame(gameId: String): Flow<StoredGame?> = game

    override fun watchPlayers(gameId: String): Flow<List<StoredMember>> = players

    override suspend fun createGame(title: String, durationMinutes: Int, maxPlayers: Int?): String {
        record("create:$title:$durationMinutes:$maxPlayers")
        return gameId
    }

    override suspend fun joinGame(code: String): String {
        record("join:$code")
        return gameId
    }

    override suspend fun leaveGame(gameId: String) = record("leave:$gameId")

    override suspend fun kickPlayer(gameId: String, playerId: String) = record("kick:$gameId:$playerId")

    override suspend fun cancelGame(gameId: String) = record("cancel:$gameId")

    private fun record(call: String) {
        calls += call
        failure?.let { exception -> throw exception }
    }
}

fun storedGame(
    masterId: String = "master",
    playerCount: Int = 1,
): StoredGame =
    StoredGame(
        id = "game",
        title = "Le manoir",
        durationMinutes = 60,
        maxPlayers = null,
        status = "WAITING",
        masterId = masterId,
        master = StoredMember(userId = masterId, username = "Maître", avatarId = 3),
        playerCount = playerCount,
        joinCode = "ABC234",
    )

fun storedGameEntry(gameId: String, isMaster: Boolean): StoredGameEntry =
    StoredGameEntry(
        gameId = gameId,
        title = "Le manoir",
        isMaster = isMaster,
        masterUsername = "Maître",
        masterAvatarId = 3,
    )
