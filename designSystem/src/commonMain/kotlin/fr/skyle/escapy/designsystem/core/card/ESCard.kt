package fr.skyle.escapy.designsystem.core.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme

/**
 * A block of content detached from the background, by its color and by its edge: its color alone is
 * too close to the background to be seen by everyone.
 */
@Composable
fun ESCard(
    style: ESCardDefaults.Style,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = ESTheme.shapes.large,
        color = style.containerColor,
        contentColor = ESTheme.colors.textPrimary,
        border = BorderStroke(width = 1.dp, color = ESTheme.colors.outlineVariant),
    ) {
        Column(content = content)
    }
}

object ESCardDefaults {

    enum class Style {
        STANDARD;

        val containerColor: Color
            @Composable
            get() = when (this) {
                STANDARD -> ESTheme.colors.surface
            }
    }
}

@Preview
@Composable
private fun PreviewESCard() {
    ESTheme {
        ESCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ESTheme.spacing.m),
            style = ESCardDefaults.Style.STANDARD,
        ) {
            ESText(
                modifier = Modifier.padding(ESTheme.spacing.m),
                text = "The cellar",
                style = ESTheme.typography.subtitle,
            )
        }
    }
}
