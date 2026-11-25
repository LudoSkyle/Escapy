package fr.skyle.escapy.designsystem.core.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * An icon that is not a button. Its [contentDescription] is null only when a text next to it already
 * says what it means.
 */
@Composable
fun ESIcon(
    painter: Painter,
    contentDescription: String?,
    size: ESIconDefaults.Size,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        modifier = modifier.size(size.dimension),
        painter = painter,
        contentDescription = contentDescription,
        tint = tint,
    )
}

object ESIconDefaults {

    enum class Size {
        SMALL,
        MEDIUM;

        val dimension: Dp
            get() = when (this) {
                SMALL -> 20.dp
                MEDIUM -> 24.dp
            }
    }
}
