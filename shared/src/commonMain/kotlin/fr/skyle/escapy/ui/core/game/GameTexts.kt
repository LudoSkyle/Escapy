package fr.skyle.escapy.ui.core.game

import androidx.compose.runtime.Composable
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.duration_hours
import fr.skyle.escapy.resources.duration_hours_minutes
import fr.skyle.escapy.resources.duration_minutes
import fr.skyle.escapy.resources.game_players
import fr.skyle.escapy.resources.game_players_of_max
import fr.skyle.escapy.resources.game_status_finished
import fr.skyle.escapy.resources.game_status_paused
import fr.skyle.escapy.resources.game_status_running
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

/** A duration as people say it: "45 min", "1 h", "1 h 30". */
@Composable
fun durationText(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> stringResource(Res.string.duration_minutes, minutes)
        rest == 0 -> stringResource(Res.string.duration_hours, hours)
        // Compose Resources replace %1$d and %1$s only, without a width: the minutes come padded
        else -> stringResource(Res.string.duration_hours_minutes, hours, rest.toString().padStart(2, '0'))
    }
}

/** How many players a game has, and its maximum when the game master set one. */
@Composable
fun playersText(playerCount: Int, maxPlayers: Int?): String =
    if (maxPlayers == null) {
        pluralStringResource(Res.plurals.game_players, playerCount, playerCount)
    } else {
        pluralStringResource(Res.plurals.game_players_of_max, playerCount, playerCount, maxPlayers)
    }

/** Where a started game is, for a list; null for a game still waiting, which needs no word. */
@Composable
fun gameStatusText(status: GameStatus): String? =
    when (status) {
        GameStatus.WAITING -> null
        GameStatus.RUNNING -> stringResource(Res.string.game_status_running)
        GameStatus.PAUSED -> stringResource(Res.string.game_status_paused)
        GameStatus.FINISHED -> stringResource(Res.string.game_status_finished)
    }

/** A timer as a clock shows it: "04:59", "1:02:03". A part of a second counts as a whole one. */
fun timerText(duration: Duration): String {
    val totalSeconds = (duration.inWholeMilliseconds + 999).coerceAtLeast(0) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val minutesAndSeconds = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    return if (hours > 0) "$hours:$minutesAndSeconds" else minutesAndSeconds
}
