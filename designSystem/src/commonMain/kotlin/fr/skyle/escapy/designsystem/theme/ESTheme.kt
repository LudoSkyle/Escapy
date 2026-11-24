package fr.skyle.escapy.designsystem.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * The theme of Escapy, around every screen. There is no MaterialTheme: each Material component is
 * wrapped in the design system, which gives it the values of ESTheme.
 */
@Composable
fun ESTheme(content: @Composable () -> Unit) {
    val colors = escapyColors
    val typography = escapyTypography()
    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTypography provides typography,
        LocalShapes provides escapyShapes,
        LocalSpacing provides escapySpacing,
        LocalIndication provides ripple(),
        // What Material reads when a component is given no value of its own
        LocalContentColor provides colors.textPrimary,
        LocalTextStyle provides typography.body,
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = colors.primary,
            backgroundColor = colors.primary.copy(alpha = 0.4f),
        ),
        content = content,
    )
}

object ESTheme {
    val colors: ESColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: ESTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val shapes: ESShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current

    val spacing: ESSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current
}
