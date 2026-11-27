package fr.skyle.escapy.domain.mapper

import fr.skyle.escapy.data.model.StoredGame
import fr.skyle.escapy.data.model.StoredGameEntry
import fr.skyle.escapy.data.model.StoredMember
import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameEnd
import fr.skyle.escapy.domain.model.GameResult
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.domain.model.GameTimer
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
