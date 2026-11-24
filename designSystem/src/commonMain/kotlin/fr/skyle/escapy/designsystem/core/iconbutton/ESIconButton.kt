package fr.skyle.escapy.designsystem.core.iconbutton

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.resources.Res
import fr.skyle.escapy.designsystem.resources.ic_arrow_back
import fr.skyle.escapy.designsystem.theme.ESTheme
import org.jetbrains.compose.resources.painterResource

/** An action shown by an icon alone: its [contentDescription] is what a screen reader says. */
@Composable
fun ESIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    style: ESIconButtonDefaults.Style,
    size: ESIconButtonDefaults.Size,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    IconButton(
        modifier = modifier.size(size.touchTarget),
        onClick = onClick,
        enabled = isEnabled,
        shape = ESTheme.shapes.full,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = style.containerColor,
            contentColor = style.contentColor,
            disabledContainerColor = style.containerColor,
            disabledContentColor = ESTheme.colors.disabledContent,
        ),
    ) {
        Icon(
            modifier = Modifier.size(size.iconSize),
            painter = icon,
            contentDescription = contentDescription,
        )
    }
}

object ESIconButtonDefaults {

    enum class Style {
        // On the background, in a top bar
        STANDARD,
        // Detached from its surface
        FILLED;

        val containerColor: Color
            @Composable
            get() = when (this) {
                STANDARD -> Color.Transparent
                FILLED -> ESTheme.colors.surfaceHigh
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                STANDARD, FILLED -> ESTheme.colors.textPrimary
            }
    }

    enum class Size {
        MEDIUM;

        // At least 48 dp, the minimum target of a touch
        val touchTarget: Dp
            get() = when (this) {
                MEDIUM -> 48.dp
            }

        val iconSize: Dp
            get() = when (this) {
                MEDIUM -> 24.dp
            }
    }
}

@Preview
@Composable
private fun PreviewESIconButton() {
    ESTheme {
        Row(modifier = Modifier.padding(ESTheme.spacing.m)) {
            ESIconButtonDefaults.Style.entries.forEach { style ->
                ESIconButton(
                    icon = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    onClick = {},
                    style = style,
                    size = ESIconButtonDefaults.Size.MEDIUM,
                )
            }
        }
    }
}
