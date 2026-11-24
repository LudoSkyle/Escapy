package fr.skyle.escapy.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** The shapes of Escapy: generous radii, each used by the components listed. */
@Immutable
data class ESShapes(
    // A snackbar
    val small: Shape,
    // A button, a field
    val medium: Shape,
    // A card, a dialog
    val large: Shape,
    // An icon button
    val full: Shape,
)

internal val escapyShapes = ESShapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    full = CircleShape,
)

internal val LocalShapes = staticCompositionLocalOf {
    ESShapes(
        small = RectangleShape,
        medium = RectangleShape,
        large = RectangleShape,
        full = RectangleShape,
    )
}
