package fr.skyle.escapy.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The spaces between and around elements, on a scale of 4 dp. */
@Immutable
data class ESSpacing(
    val xxs: Dp,
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp,
    val xxl: Dp,
)

internal val escapySpacing = ESSpacing(
    xxs = 4.dp,
    xs = 8.dp,
    s = 12.dp,
    m = 16.dp,
    l = 24.dp,
    xl = 32.dp,
    xxl = 48.dp,
)

internal val LocalSpacing = staticCompositionLocalOf { escapySpacing }
