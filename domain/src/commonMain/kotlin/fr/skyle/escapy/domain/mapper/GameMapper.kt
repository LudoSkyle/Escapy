package fr.skyle.escapy.domain.mapper

import fr.skyle.escapy.data.model.StoredGame
import fr.skyle.escapy.data.model.StoredGameEntry
import fr.skyle.escapy.data.model.StoredMember
import fr.skyle.escapy.domain.model.Game
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameSummary

internal fun StoredGame.toGame(currentUserId: String): Game =
    Game(
        id = id,
        title = title,
        durationMinutes = durationMinutes,
        maxPlayers = maxPlayers,
        master = master.toGameMember(currentUserId = currentUserId),
        playerCount = playerCount,
        joinCode = joinCode,
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
    )
