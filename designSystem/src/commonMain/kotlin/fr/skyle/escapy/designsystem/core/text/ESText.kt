package fr.skyle.escapy.designsystem.core.text

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.theme.ESTheme

/**
 * A text in a [style] of the theme (ESTheme.typography), in the color of the content around it
 * unless given a [color] of the theme.
 */
@Composable
fun ESText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

/**
 * A text of several styles, such as a name in the accent color within a sentence: its spans hold
 * colors of the theme only.
 */
@Composable
fun ESText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Preview
@Composable
private fun PreviewESText() {
    ESTheme {
        Column(modifier = Modifier.padding(ESTheme.spacing.m)) {
            ESText(text = "Headline", style = ESTheme.typography.headline)
            ESText(text = "Title", style = ESTheme.typography.title)
            ESText(text = "Subtitle", style = ESTheme.typography.subtitle)
            ESText(text = "Body", style = ESTheme.typography.body)
            ESText(text = "Body small", style = ESTheme.typography.bodySmall, color = ESTheme.colors.textSecondary)
            ESText(text = "Label", style = ESTheme.typography.label)
            ESText(text = "Caption", style = ESTheme.typography.caption, color = ESTheme.colors.textTertiary)
        }
    }
}
