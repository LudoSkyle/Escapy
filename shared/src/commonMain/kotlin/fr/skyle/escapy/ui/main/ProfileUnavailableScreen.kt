package fr.skyle.escapy.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_retry
import fr.skyle.escapy.resources.profile_unavailable_message
import fr.skyle.escapy.resources.profile_unavailable_title
import fr.skyle.escapy.resources.sign_out
import org.jetbrains.compose.resources.stringResource

/** Signed in, but the profile could not be read nor created, most often without network. */
@Composable
fun ProfileUnavailableScreen(
    onRetryClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
) {
    ESScreenStructure(style = ESScreenStructureDefaults.Style.STANDARD) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(ESTheme.spacing.l),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            ESText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.profile_unavailable_title),
                style = ESTheme.typography.title,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.s))

            ESText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.profile_unavailable_message),
                style = ESTheme.typography.body,
                color = ESTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.action_retry),
                onClick = onRetryClicked,
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.LARGE,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.sign_out),
                onClick = onSignOutClicked,
                style = ESButtonDefaults.Style.TEXT,
                size = ESButtonDefaults.Size.MEDIUM,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewProfileUnavailableScreen() {
    ESTheme {
        ProfileUnavailableScreen(
            onRetryClicked = {},
            onSignOutClicked = {},
        )
    }
}
