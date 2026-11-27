package fr.skyle.escapy.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** A game the user runs or plays, as its members see it. */
data class Game(
    val id: String,
    val title: String,
    val durationMinutes: Int,
    // Null when the game master set none: the server lets in up to GameRules.PLAYER_LIMIT players
    val maxPlayers: Int?,
    val master: GameMember,
    val playerCount: Int,
    // Shown to the members, who may give it to the next players
    val joinCode: String,
    val status: GameStatus,
    val timer: GameTimer,
    // Once the game is over
    val end: GameEnd?,
) {
    val isMaster: Boolean
        get() = master.isCurrentUser

    /**
     * The time left at [serverNow], the time of the server as the device estimates it: the full
     * duration before the game starts, counted down while it runs, still while it is paused.
     */
    fun remainingAt(serverNow: Instant): Duration =
        when (status) {
            GameStatus.WAITING -> durationMinutes.minutes
            GameStatus.RUNNING -> timer.endsAt?.let { endsAt -> (endsAt - serverNow).coerceAtLeast(Duration.ZERO) } ?: Duration.ZERO
            GameStatus.PAUSED, GameStatus.FINISHED -> timer.remaining ?: Duration.ZERO
        }
}

/** Where a game is: waiting for its players, its timer running or paused, then over for good. */
enum class GameStatus {
    WAITING,
    RUNNING,
    PAUSED,
    FINISHED,
}

/** The timer of a started game, as the server keeps it. */
data class GameTimer(
    // When the timer reaches zero, by the clock of the server, while the game runs
    val endsAt: Instant?,
    // The time left while the game is paused, or once it is over
    val remaining: Duration?,
    // The minutes the game master added
    val addedMinutes: Int,
)

/** How a game ended, and what its recap shows. */
data class GameEnd(
    val result: GameResult,
    // The time the game was played for, pauses apart
    val played: Duration,
)

enum class GameResult {
    // The timer reached zero
    TIME_UP,

    // The game master ended the game before
    ENDED_BY_MASTER,
}

/** The game master or a player of a game, with the name and avatar copied from their profile. */
data class GameMember(
    val userId: String,
    val username: String,
    val avatarId: Int,
    val isCurrentUser: Boolean,
)
