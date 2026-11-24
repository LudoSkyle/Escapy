package fr.skyle.escapy.designsystem.core.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

/** The text styles of Escapy, so the screens never reach Material 3 themselves. */
enum class ESTextStyle {
    Title,
    Body,
}

@Composable
fun ESText(
    text: String,
    modifier: Modifier = Modifier,
    style: ESTextStyle = ESTextStyle.Body,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        style = when (style) {
            ESTextStyle.Title -> MaterialTheme.typography.headlineMedium
            ESTextStyle.Body -> MaterialTheme.typography.bodyLarge
        },
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = textAlign,
    )
}
