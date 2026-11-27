package fr.skyle.escapy.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class GameTest {

    private val now = Instant.fromEpochMilliseconds(10_000_000)

    private fun game(status: GameStatus, timer: GameTimer = GameTimer(endsAt = null, remaining = null, addedMinutes = 0)) =
        Game(
            id = "game",
            title = "Le manoir",
            durationMinutes = 60,
            maxPlayers = null,
            master = GameMember(userId = "master", username = "Maître", avatarId = 3, isCurrentUser = true),
            playerCount = 1,
            joinCode = "ABC234",
            status = status,
            timer = timer,
            end = null,
        )

    @Test
    fun `a waiting game has its whole duration left`() {
        assertEquals(expected = 60.minutes, actual = game(GameStatus.WAITING).remainingAt(now))
    }

    @Test
    fun `a running game counts down to its end and stops at zero`() {
        // Given
        val running = game(GameStatus.RUNNING, GameTimer(endsAt = now + 90.seconds, remaining = null, addedMinutes = 0))

        // Then
        assertEquals(expected = 90.seconds, actual = running.remainingAt(now))
        assertEquals(expected = Duration.ZERO, actual = running.remainingAt(now + 2.minutes))
    }

    @Test
    fun `a paused game keeps its time left whatever the time`() {
        // Given
        val paused = game(GameStatus.PAUSED, GameTimer(endsAt = null, remaining = 5.minutes, addedMinutes = 0))

        // Then
        assertEquals(expected = 5.minutes, actual = paused.remainingAt(now + 1.minutes))
    }
}
