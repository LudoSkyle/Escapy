package fr.skyle.escapy.ui.screen.welcome.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.text.ESTextStyle
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.app_name
import fr.skyle.escapy.resources.welcome_coming_soon
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen() {
    ESScreenStructure(contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ESText(text = stringResource(Res.string.app_name), style = ESTextStyle.Title)
            ESText(text = stringResource(Res.string.welcome_coming_soon), textAlign = TextAlign.Center)
        }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    ESTheme {
        WelcomeScreen()
    }
}
