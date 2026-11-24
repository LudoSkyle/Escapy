package fr.skyle.escapy.designsystem.core.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.theme.ESTheme

object ESButtonDefaults {

    enum class Style {
        // The main action of a screen, once per screen
        PRIMARY,
        // Another action, outlined
        SECONDARY,
        // A light action, a text only (in a dialog, under a form)
        TEXT,
        // An action that deletes or loses something
        DESTRUCTIVE;

        val containerColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> ESTheme.colors.primary
                SECONDARY, TEXT -> Color.Transparent
                DESTRUCTIVE -> ESTheme.colors.error
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> ESTheme.colors.onPrimary
                SECONDARY -> ESTheme.colors.textPrimary
                TEXT -> ESTheme.colors.primary
                DESTRUCTIVE -> ESTheme.colors.onError
            }

        val disabledContainerColor: Color
            @Composable
            get() = when (this) {
                PRIMARY, DESTRUCTIVE -> ESTheme.colors.disabledContainer
                SECONDARY, TEXT -> Color.Transparent
            }

        val border: BorderStroke?
            @Composable
            get() = when (this) {
                SECONDARY -> BorderStroke(width = 1.dp, color = ESTheme.colors.outline)
                PRIMARY, TEXT, DESTRUCTIVE -> null
            }
    }

    enum class Size {
        MEDIUM,
        LARGE;

        // A minimum: the button grows with a larger font
        val minHeight: Dp
            get() = when (this) {
                MEDIUM -> 48.dp
                LARGE -> 56.dp
            }

        val contentPadding: PaddingValues
            get() = when (this) {
                MEDIUM -> PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                LARGE -> PaddingValues(horizontal = 24.dp, vertical = 14.dp)
            }
    }
}
