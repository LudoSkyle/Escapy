package fr.skyle.escapy.designsystem.core.progress

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.theme.ESTheme

/** Something in progress, for an unknown time. */
@Composable
fun ESProgressIndicator(
    style: ESProgressIndicatorDefaults.Style,
    size: ESProgressIndicatorDefaults.Size,
    modifier: Modifier = Modifier,
    color: Color = style.color,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size.diameter),
        color = color,
        trackColor = Color.Transparent,
        strokeWidth = size.strokeWidth,
    )
}

object ESProgressIndicatorDefaults {

    enum class Style {
        PRIMARY;

        val color: Color
            @Composable
            get() = when (this) {
                PRIMARY -> ESTheme.colors.primary
            }
    }

    enum class Size {
        SMALL,
        LARGE;

        val diameter: Dp
            get() = when (this) {
                SMALL -> 20.dp
                LARGE -> 40.dp
            }

        val strokeWidth: Dp
            get() = when (this) {
                SMALL -> 2.dp
                LARGE -> 4.dp
            }
    }
}
