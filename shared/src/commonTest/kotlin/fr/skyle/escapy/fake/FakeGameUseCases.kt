package fr.skyle.escapy.fake

import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.usecase.game.CancelGameUseCase
import fr.skyle.escapy.domain.usecase.game.CreateGameUseCase
import fr.skyle.escapy.domain.usecase.game.JoinGameUseCase
import fr.skyle.escapy.domain.usecase.game.KickPlayerUseCase
import fr.skyle.escapy.domain.usecase.game.LeaveGameUseCase
import fr.skyle.escapy.domain.usecase.game.WatchGameUseCase
import fr.skyle.escapy.domain.usecase.game.WatchMyGamesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeWatchMyGamesUseCase : WatchMyGamesUseCase {

    val games = MutableSharedFlow<List<Game>>()

    override fun invoke(): Flow<List<Game>> = games
}

class FakeWatchGameUseCase : WatchGameUseCase {

    val states = MutableSharedFlow<GameState>()

    override fun invoke(gameId: String): Flow<GameState> = states
}

class FakeCreateGameUseCase : FakeAction(), CreateGameUseCase {
    override suspend fun invoke(title: String, durationMinutes: Int, maxPlayers: Int?): String {
        record("$title:$durationMinutes:$maxPlayers")
        return "game"
    }
}

class FakeJoinGameUseCase : FakeAction(), JoinGameUseCase {
    override suspend fun invoke(code: String): String {
        record(code)
        return "game"
    }
}

class FakeLeaveGameUseCase : FakeAction(), LeaveGameUseCase {
    override suspend fun invoke(gameId: String) = record(gameId)
}

class FakeKickPlayerUseCase : FakeAction(), KickPlayerUseCase {
    override suspend fun invoke(gameId: String, playerId: String) = record("$gameId:$playerId")
}

class FakeCancelGameUseCase : FakeAction(), CancelGameUseCase {
    override suspend fun invoke(gameId: String) = record(gameId)
}

fun gameMember(userId: String, isCurrentUser: Boolean = false): GameMember =
    GameMember(userId = userId, username = "Name of $userId", avatarId = 3, isCurrentUser = isCurrentUser)

fun game(isMaster: Boolean): Game =
    Game(
        id = "game",
        title = "Le manoir",
        durationMinutes = 60,
        maxPlayers = null,
        master = gameMember(userId = "master", isCurrentUser = isMaster),
        playerCount = 1,
        joinCode = "ABC234",
    )
