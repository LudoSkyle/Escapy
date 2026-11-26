package fr.skyle.escapy.ui.core.game

import androidx.compose.runtime.Composable
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.duration_hours
import fr.skyle.escapy.resources.duration_hours_minutes
import fr.skyle.escapy.resources.duration_minutes
import fr.skyle.escapy.resources.game_players
import fr.skyle.escapy.resources.game_players_of_max
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

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
