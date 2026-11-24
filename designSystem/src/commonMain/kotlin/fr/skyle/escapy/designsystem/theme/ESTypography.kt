package fr.skyle.escapy.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import fr.skyle.escapy.designsystem.resources.Res
import fr.skyle.escapy.designsystem.resources.exo2_bold
import fr.skyle.escapy.designsystem.resources.exo2_light
import fr.skyle.escapy.designsystem.resources.exo2_medium
import fr.skyle.escapy.designsystem.resources.exo2_regular
import fr.skyle.escapy.designsystem.resources.exo2_semibold
import org.jetbrains.compose.resources.Font

@Composable
private fun exo2FontFamily(): FontFamily =
    FontFamily(
        Font(resource = Res.font.exo2_light, weight = FontWeight.Light),
        Font(resource = Res.font.exo2_regular, weight = FontWeight.Normal),
        Font(resource = Res.font.exo2_medium, weight = FontWeight.Medium),
        Font(resource = Res.font.exo2_semibold, weight = FontWeight.SemiBold),
        Font(resource = Res.font.exo2_bold, weight = FontWeight.Bold),
    )

// Every style of Material 3 in Exo 2, with its default sizes until the design system is redone
@Composable
internal fun esTypography(): Typography {
    val exo2 = exo2FontFamily()
    val defaults = Typography()
    fun TextStyle.inExo2(): TextStyle = copy(fontFamily = exo2)
    return Typography(
        displayLarge = defaults.displayLarge.inExo2(),
        displayMedium = defaults.displayMedium.inExo2(),
        displaySmall = defaults.displaySmall.inExo2(),
        headlineLarge = defaults.headlineLarge.inExo2(),
        headlineMedium = defaults.headlineMedium.inExo2(),
        headlineSmall = defaults.headlineSmall.inExo2(),
        titleLarge = defaults.titleLarge.inExo2(),
        titleMedium = defaults.titleMedium.inExo2(),
        titleSmall = defaults.titleSmall.inExo2(),
        bodyLarge = defaults.bodyLarge.inExo2(),
        bodyMedium = defaults.bodyMedium.inExo2(),
        bodySmall = defaults.bodySmall.inExo2(),
        labelLarge = defaults.labelLarge.inExo2(),
        labelMedium = defaults.labelMedium.inExo2(),
        labelSmall = defaults.labelSmall.inExo2(),
    )
}
