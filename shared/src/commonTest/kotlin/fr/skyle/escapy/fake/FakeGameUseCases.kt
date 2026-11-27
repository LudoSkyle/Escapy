package fr.skyle.escapy.fake

import fr.skyle.escapy.domain.model.ChatMessage
import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameEnd
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.GameSummary
import fr.skyle.escapy.domain.model.GameTimer
import fr.skyle.escapy.domain.usecase.chat.SendMessageUseCase
import fr.skyle.escapy.domain.usecase.chat.WatchMessagesUseCase
import fr.skyle.escapy.domain.usecase.clock.EstimateServerClockOffsetUseCase
import fr.skyle.escapy.domain.usecase.game.AddTimeUseCase
import fr.skyle.escapy.domain.usecase.game.CancelGameUseCase
import fr.skyle.escapy.domain.usecase.game.CreateGameUseCase
import fr.skyle.escapy.domain.usecase.game.EndGameUseCase
import fr.skyle.escapy.domain.usecase.game.ExpireGameUseCase
import fr.skyle.escapy.domain.usecase.game.JoinGameUseCase
import fr.skyle.escapy.domain.usecase.game.KickPlayerUseCase
import fr.skyle.escapy.domain.usecase.game.LeaveGameUseCase
import fr.skyle.escapy.domain.usecase.game.PauseGameUseCase
import fr.skyle.escapy.domain.usecase.game.ResumeGameUseCase
import fr.skyle.escapy.domain.usecase.game.StartGameUseCase
import fr.skyle.escapy.domain.usecase.game.WatchGameUseCase
import fr.skyle.escapy.domain.usecase.game.WatchMyGamesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

class FakeWatchMyGamesUseCase : WatchMyGamesUseCase {

    val games = MutableSharedFlow<List<GameSummary>>()

    override fun invoke(): Flow<List<GameSummary>> = games
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

class FakeStartGameUseCase : FakeAction(), StartGameUseCase {
    override suspend fun invoke(gameId: String) = record(gameId)
}

class FakePauseGameUseCase : FakeAction(), PauseGameUseCase {
    override suspend fun invoke(gameId: String) = record(gameId)
}

class FakeResumeGameUseCase : FakeAction(), ResumeGameUseCase {
    override suspend fun invoke(gameId: String) = record(gameId)
}

class FakeAddTimeUseCase : FakeAction(), AddTimeUseCase {
    override suspend fun invoke(gameId: String, minutes: Int, addedMinutes: Int) = record("$gameId:$minutes:$addedMinutes")
}

class FakeEndGameUseCase : FakeAction(), EndGameUseCase {
    override suspend fun invoke(gameId: String) = record(gameId)
}

class FakeExpireGameUseCase : FakeAction(), ExpireGameUseCase {
    override suspend fun invoke(gameId: String) = record(gameId)
}

/** Estimates the offsets given, in turn, or fails with [failure] once they are told. */
class FakeEstimateServerClockOffsetUseCase(
    var offsets: List<Duration> = listOf(Duration.ZERO),
    var failure: Exception? = null,
) : EstimateServerClockOffsetUseCase {

    var calls = 0

    override fun invoke(): Flow<Duration> = flow {
        calls++
        offsets.forEach { offset -> emit(offset) }
        failure?.let { exception -> throw exception }
    }
}

class FakeWatchMessagesUseCase : WatchMessagesUseCase {

    val messages = MutableSharedFlow<List<ChatMessage>>()

    override fun invoke(gameId: String): Flow<List<ChatMessage>> = messages
}

class FakeSendMessageUseCase : FakeAction(), SendMessageUseCase {
    override suspend fun invoke(gameId: String, text: String) = record("$gameId:$text")
}

fun chatMessage(id: String, isMine: Boolean = false, text: String = "Message $id"): ChatMessage =
    ChatMessage(
        id = id,
        authorId = if (isMine) "me" else "master",
        authorUsername = if (isMine) "Joueuse" else "Maître",
        authorAvatarId = 3,
        text = text,
        sentAt = null,
        isMine = isMine,
        isPending = false,
    )

fun gameMember(userId: String, isCurrentUser: Boolean = false): GameMember =
    GameMember(userId = userId, username = "Name of $userId", avatarId = 3, isCurrentUser = isCurrentUser)

fun game(
    isMaster: Boolean,
    status: GameStatus = GameStatus.WAITING,
    timer: GameTimer = GameTimer(endsAt = null, remaining = null, addedMinutes = 0),
    end: GameEnd? = null,
): Game =
    Game(
        id = "game",
        title = "Le manoir",
        durationMinutes = 60,
        maxPlayers = null,
        master = gameMember(userId = "master", isCurrentUser = isMaster),
        playerCount = 1,
        joinCode = "ABC234",
        status = status,
        timer = timer,
        end = end,
    )

fun gameSummary(isMaster: Boolean, status: GameStatus = GameStatus.WAITING): GameSummary =
    GameSummary(
        gameId = "game",
        title = "Le manoir",
        isMaster = isMaster,
        masterUsername = "Maître",
        masterAvatarId = 3,
        status = status,
    )
