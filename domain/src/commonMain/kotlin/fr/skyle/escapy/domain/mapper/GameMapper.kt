package fr.skyle.escapy.domain.mapper

import fr.skyle.escapy.data.model.StoredGame
import fr.skyle.escapy.data.model.StoredMessage
import fr.skyle.escapy.data.model.StoredMission
import fr.skyle.escapy.data.model.StoredGameEntry
import fr.skyle.escapy.data.model.StoredMember
import fr.skyle.escapy.domain.model.ChatMessage
import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameEnd
import fr.skyle.escapy.domain.model.GameResult
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.GameTimer
import fr.skyle.escapy.domain.model.Mission
import fr.skyle.escapy.domain.model.MissionSolver
import fr.skyle.escapy.domain.model.MissionState
import fr.skyle.escapy.domain.model.MissionTrigger
import fr.skyle.escapy.domain.model.MissionType
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameSummary
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

internal fun StoredGame.toGame(currentUserId: String): Game =
    Game(
        id = id,
        title = title,
        durationMinutes = durationMinutes,
        maxPlayers = maxPlayers,
        master = master.toGameMember(currentUserId = currentUserId),
        playerCount = playerCount,
        joinCode = joinCode,
        status = status.toGameStatus(),
        timer = GameTimer(
            endsAt = endsAtMs?.let { millis -> Instant.fromEpochMilliseconds(millis) },
            remaining = remainingMs?.milliseconds,
            addedMinutes = addedMinutes,
        ),
        end = result?.toGameResult()?.let { gameResult ->
            GameEnd(result = gameResult, played = (playedMs ?: 0L).milliseconds)
        },
    )

internal fun StoredMember.toGameMember(currentUserId: String): GameMember =
    GameMember(
        userId = userId,
        username = username,
        avatarId = avatarId,
        isCurrentUser = userId == currentUserId,
    )

internal fun StoredGameEntry.toGameSummary(): GameSummary =
    GameSummary(
        gameId = gameId,
        title = title,
        isMaster = isMaster,
        masterUsername = masterUsername,
        masterAvatarId = masterAvatarId,
        status = status.toGameStatus(),
    )

// A status this version does not know, from a newer server, reads as a game not started
private fun String.toGameStatus(): GameStatus =
    GameStatus.entries.firstOrNull { status -> status.name == this } ?: GameStatus.WAITING

private fun String.toGameResult(): GameResult? =
    GameResult.entries.firstOrNull { result -> result.name == this }

internal fun StoredMessage.toChatMessage(currentUserId: String): ChatMessage =
    ChatMessage(
        id = id,
        authorId = authorId,
        authorUsername = authorUsername,
        authorAvatarId = authorAvatarId,
        text = text,
        sentAt = sentAtMs?.let { millis -> Instant.fromEpochMilliseconds(millis) },
        isMine = authorId == currentUserId,
        isPending = isPending,
    )

internal fun StoredMission.toMission(currentUserId: String): Mission =
    Mission(
        id = id,
        title = title,
        statement = statement,
        type = MissionType.entries.firstOrNull { missionType -> missionType.name == type } ?: MissionType.TEXT,
        codeLength = codeLength,
        isFinal = isFinal,
        state = MissionState.entries.firstOrNull { missionState -> missionState.name == state } ?: MissionState.LOCKED,
        solver = solverId?.let { userId ->
            MissionSolver(userId = userId, username = solverUsername.orEmpty(), isCurrentUser = userId == currentUserId)
        },
        answers = answers,
        trigger = when (triggerType) {
            null -> null
            "ON_START" -> MissionTrigger.OnStart
            "AFTER_MISSION" -> triggerMissionId?.let { missionId -> MissionTrigger.AfterMission(missionId = missionId) } ?: MissionTrigger.Manual
            else -> MissionTrigger.Manual
        },
    )
