package fr.skyle.escapy.fake

import fr.skyle.escapy.domain.model.ChatMessage
import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameEnd
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.GameSummary
import fr.skyle.escapy.domain.model.GameTimer
import fr.skyle.escapy.domain.model.Mission
import fr.skyle.escapy.domain.model.MissionDraft
import fr.skyle.escapy.domain.model.MissionState
import fr.skyle.escapy.domain.model.MissionTrigger
import fr.skyle.escapy.domain.model.MissionType
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
import fr.skyle.escapy.domain.usecase.mission.DeleteMissionUseCase
import fr.skyle.escapy.domain.usecase.mission.SaveMissionUseCase
import fr.skyle.escapy.domain.usecase.mission.SubmitAnswerUseCase
import fr.skyle.escapy.domain.usecase.mission.UnlockMissionUseCase
import fr.skyle.escapy.domain.usecase.mission.ValidateMissionUseCase
import fr.skyle.escapy.domain.usecase.mission.WatchMissionsUseCase
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

class FakeWatchMissionsUseCase : WatchMissionsUseCase {

    val missions = MutableSharedFlow<List<Mission>>()

    /** The roles the missions were asked for: true for the game master. */
    val asMaster = mutableListOf<Boolean>()

    override fun invoke(gameId: String, isMaster: Boolean): Flow<List<Mission>> {
        asMaster += isMaster
        return missions
    }
}

class FakeSubmitAnswerUseCase : FakeAction(), SubmitAnswerUseCase {

    var isCorrect = false

    override suspend fun invoke(gameId: String, missionId: String, answer: String): Boolean {
        record("$missionId:$answer")
        return isCorrect
    }
}

class FakeUnlockMissionUseCase : FakeAction(), UnlockMissionUseCase {
    override suspend fun invoke(gameId: String, missionId: String) = record(missionId)
}

class FakeValidateMissionUseCase : FakeAction(), ValidateMissionUseCase {
    override suspend fun invoke(gameId: String, missionId: String) = record(missionId)
}

class FakeSaveMissionUseCase : FakeAction(), SaveMissionUseCase {

    val drafts = mutableListOf<MissionDraft>()

    override suspend fun invoke(gameId: String, draft: MissionDraft): String {
        drafts += draft
        record(draft.title)
        return draft.missionId ?: "new"
    }
}

class FakeDeleteMissionUseCase : FakeAction(), DeleteMissionUseCase {
    override suspend fun invoke(gameId: String, missionId: String) = record(missionId)
}

fun mission(
    id: String,
    state: MissionState = MissionState.ACTIVE,
    trigger: MissionTrigger? = null,
    answers: List<String> = emptyList(),
): Mission =
    Mission(
        id = id,
        title = "Mission $id",
        statement = "Trouvez le code.",
        type = MissionType.CODE,
        codeLength = 4,
        isFinal = false,
        state = state,
        solver = null,
        answers = answers,
        trigger = trigger,
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
