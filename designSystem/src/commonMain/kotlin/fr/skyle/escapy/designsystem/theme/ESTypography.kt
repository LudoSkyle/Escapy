package fr.skyle.escapy.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fr.skyle.escapy.designsystem.resources.Res
import fr.skyle.escapy.designsystem.resources.exo2_bold
import fr.skyle.escapy.designsystem.resources.exo2_light
import fr.skyle.escapy.designsystem.resources.exo2_medium
import fr.skyle.escapy.designsystem.resources.exo2_regular
import fr.skyle.escapy.designsystem.resources.exo2_semibold
import org.jetbrains.compose.resources.Font

/** The text styles of Escapy, all in Exo 2, each line taller than its text. */
@Immutable
data class ESTypography(
    // A figure read from across the room: the timer of a game, with figures of one width
    val display: TextStyle,
    // The title of a screen that stands alone (welcome, victory)
    val headline: TextStyle,
    // The title of a screen, of a dialog
    val title: TextStyle,
    // The title of a card, of a section
    val subtitle: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    // The text of a button
    val label: TextStyle,
    // Under a field, beside a value
    val caption: TextStyle,
)

@Composable
private fun exo2(): FontFamily =
    FontFamily(
        Font(resource = Res.font.exo2_light, weight = FontWeight.Light),
        Font(resource = Res.font.exo2_regular, weight = FontWeight.Normal),
        Font(resource = Res.font.exo2_medium, weight = FontWeight.Medium),
        Font(resource = Res.font.exo2_semibold, weight = FontWeight.SemiBold),
        Font(resource = Res.font.exo2_bold, weight = FontWeight.Bold),
    )

// Read from Compose Resources, which only a composable can do
@Composable
internal fun escapyTypography(): ESTypography {
    val exo2 = exo2()
    return ESTypography(
        display = TextStyle(
            fontFamily = exo2,
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp,
            lineHeight = 72.sp,
            // Tabular figures: a timer that counts down does not shift from one second to the next
            fontFeatureSettings = "tnum",
        ),
        headline = TextStyle(fontFamily = exo2, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
        title = TextStyle(fontFamily = exo2, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
        subtitle = TextStyle(fontFamily = exo2, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
        body = TextStyle(fontFamily = exo2, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodySmall = TextStyle(fontFamily = exo2, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        label = TextStyle(fontFamily = exo2, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
        caption = TextStyle(fontFamily = exo2, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    )
}

internal val LocalTypography = staticCompositionLocalOf {
    ESTypography(
        display = TextStyle.Default,
        headline = TextStyle.Default,
        title = TextStyle.Default,
        subtitle = TextStyle.Default,
        body = TextStyle.Default,
        bodySmall = TextStyle.Default,
        label = TextStyle.Default,
        caption = TextStyle.Default,
    )
}
