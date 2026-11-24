package fr.skyle.escapy.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** The theme of Escapy, around every screen. */
@Composable
fun ESTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ESColorScheme,
        typography = esTypography(),
        content = content,
    )
}
