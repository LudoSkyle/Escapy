package fr.skyle.escapy.designsystem.core.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicator
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicatorDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme

/**
 * An action. While [isLoading], it keeps its size and colors, shows its progress instead of its
 * text, ignores a touch and reads as disabled: an action is never sent twice.
 */
@Composable
fun ESButton(
    text: String,
    onClick: () -> Unit,
    style: ESButtonDefaults.Style,
    size: ESButtonDefaults.Size,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: Painter? = null,
    // False for a logo in its own colors (Google): the icon is drawn as it is
    isLeadingIconTinted: Boolean = true,
) {
    Button(
        modifier = modifier
            .heightIn(min = size.minHeight)
            .semantics {
                if (isLoading) {
                    disabled()
                }
            },
        onClick = {
            if (!isLoading) {
                onClick()
            }
        },
        enabled = isEnabled,
        shape = ESTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = style.containerColor,
            contentColor = style.contentColor,
            disabledContainerColor = style.disabledContainerColor,
            disabledContentColor = ESTheme.colors.disabledContent,
        ),
        border = style.border,
        contentPadding = size.contentPadding,
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Laid out while loading too, invisible: the button keeps its width
            Row(
                modifier = Modifier.alpha(if (isLoading) 0f else 1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = leadingIcon,
                        contentDescription = null,
                        tint = if (isLeadingIconTinted) LocalContentColor.current else Color.Unspecified,
                    )

                    Spacer(modifier = Modifier.width(ESTheme.spacing.xs))
                }

                ESText(
                    text = text,
                    style = ESTheme.typography.label,
                    textAlign = TextAlign.Center,
                )
            }

            if (isLoading) {
                ESProgressIndicator(
                    style = ESProgressIndicatorDefaults.Style.PRIMARY,
                    size = ESProgressIndicatorDefaults.Size.SMALL,
                    color = style.contentColor,
                )
            }
        }
    }
}

private class ESButtonVariantProvider : PreviewParameterProvider<Pair<ESButtonDefaults.Style, ESButtonDefaults.Size>> {
    override val values =
        ESButtonDefaults.Style.entries.flatMap { style ->
            ESButtonDefaults.Size.entries.map { size ->
                style to size
            }
        }.asSequence()
}

@Preview
@Composable
private fun PreviewESButton(
    @PreviewParameter(ESButtonVariantProvider::class) variant: Pair<ESButtonDefaults.Style, ESButtonDefaults.Size>,
) {
    ESTheme {
        Column(modifier = Modifier.padding(ESTheme.spacing.m)) {
            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Join the game",
                onClick = {},
                style = variant.first,
                size = variant.second,
            )

            Spacer(modifier = Modifier.width(ESTheme.spacing.xs))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Join the game",
                onClick = {},
                style = variant.first,
                size = variant.second,
                isLoading = true,
            )

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Join the game",
                onClick = {},
                style = variant.first,
                size = variant.second,
                isEnabled = false,
            )
        }
    }
}
