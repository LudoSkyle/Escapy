package fr.skyle.escapy.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The colors of Escapy, dark only, around a single bright accent: the amber. Every pair a component
 * draws is checked against WCAG AA by ESColorsContrastTest: 4.5:1 for a text, 3:1 for the boundary
 * of a control.
 */
@Immutable
data class ESColors(
    // Surfaces, from the furthest to the nearest: the screen, a card on it, a control on a card,
    // a control pressed or selected
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val surfaceHighest: Color,
    // The boundary of a control (a field), and a decorative line (a divider, the edge of a card)
    val outline: Color,
    val outlineVariant: Color,
    // Texts: primary everywhere, secondary for what explains, tertiary for a hint or a placeholder,
    // never on surfaceHighest
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    // The accent, and what is drawn on it
    val primary: Color,
    val onPrimary: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    // A control that cannot be used: exempt from the contrast ratios, still readable
    val disabledContainer: Color,
    val disabledContent: Color,
    // Behind a dialog or a sheet
    val scrim: Color,
)

internal val escapyColors = ESColors(
    background = Color(0xFF121416),
    surface = Color(0xFF1E2225),
    surfaceHigh = Color(0xFF2A2F33),
    surfaceHighest = Color(0xFF363C41),
    outline = Color(0xFF7C8388),
    outlineVariant = Color(0xFF3A4045),
    textPrimary = Color(0xFFF3F1EC),
    textSecondary = Color(0xFFB8BCBF),
    textTertiary = Color(0xFF9AA0A5),
    primary = Color(0xFFF5B856),
    onPrimary = Color(0xFF121416),
    error = Color(0xFFFF8576),
    onError = Color(0xFF121416),
    success = Color(0xFF5CCB8F),
    disabledContainer = Color(0xFF2A2F33),
    disabledContent = Color(0xFF6B7277),
    scrim = Color(0x99000000),
)

internal val LocalColors = staticCompositionLocalOf {
    ESColors(
        background = Color.Unspecified,
        surface = Color.Unspecified,
        surfaceHigh = Color.Unspecified,
        surfaceHighest = Color.Unspecified,
        outline = Color.Unspecified,
        outlineVariant = Color.Unspecified,
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        textTertiary = Color.Unspecified,
        primary = Color.Unspecified,
        onPrimary = Color.Unspecified,
        error = Color.Unspecified,
        onError = Color.Unspecified,
        success = Color.Unspecified,
        disabledContainer = Color.Unspecified,
        disabledContent = Color.Unspecified,
        scrim = Color.Unspecified,
    )
}
