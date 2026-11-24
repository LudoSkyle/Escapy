package fr.skyle.escapy.ui.screen.home.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.home_coming_soon
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen() {
    ESScreenStructure(contentAlignment = Alignment.Center) {
        ESText(
            text = stringResource(Res.string.home_coming_soon),
            modifier = Modifier.padding(24.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    ESTheme {
        HomeScreen()
    }
}
