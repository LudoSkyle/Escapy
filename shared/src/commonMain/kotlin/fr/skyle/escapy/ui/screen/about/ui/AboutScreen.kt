package fr.skyle.escapy.ui.screen.about.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.divider.ESDivider
import fr.skyle.escapy.designsystem.core.row.ESRow
import fr.skyle.escapy.designsystem.core.row.ESRowDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.about_description
import fr.skyle.escapy.resources.about_source_code
import fr.skyle.escapy.resources.about_title
import fr.skyle.escapy.resources.about_version
import fr.skyle.escapy.resources.app_name
import fr.skyle.escapy.resources.logo_keyhole
import fr.skyle.escapy.resources.profile_privacy_policy
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AboutScreen(
    appVersion: String,
    onBackClicked: () -> Unit,
    onSourceCodeClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.about_title),
                onBackClick = onBackClicked,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(ESTheme.spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier.size(120.dp),
                painter = painterResource(Res.drawable.logo_keyhole),
                contentDescription = null,
            )

            ESText(
                text = stringResource(Res.string.app_name),
                style = ESTheme.typography.title,
            )

            ESText(
                text = stringResource(Res.string.about_version, appVersion),
                style = ESTheme.typography.bodySmall,
                color = ESTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.m))

            ESText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.about_description),
                style = ESTheme.typography.body,
                color = ESTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESCard(
                modifier = Modifier.fillMaxWidth(),
                style = ESCardDefaults.Style.STANDARD,
            ) {
                ESRow(
                    label = stringResource(Res.string.about_source_code),
                    onClick = onSourceCodeClicked,
                    style = ESRowDefaults.Style.STANDARD,
                )

                ESDivider()

                ESRow(
                    label = stringResource(Res.string.profile_privacy_policy),
                    onClick = onPrivacyPolicyClicked,
                    style = ESRowDefaults.Style.STANDARD,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAboutScreen() {
    ESTheme {
        AboutScreen(
            appVersion = "2.0 (20000)",
            onBackClicked = {},
            onSourceCodeClicked = {},
            onPrivacyPolicyClicked = {},
        )
    }
}
