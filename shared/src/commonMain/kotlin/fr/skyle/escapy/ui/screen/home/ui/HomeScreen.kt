package fr.skyle.escapy.ui.screen.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.dialog.ESDialog
import fr.skyle.escapy.designsystem.core.dialog.ESDialogDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_cancel
import fr.skyle.escapy.resources.home_coming_soon
import fr.skyle.escapy.resources.sign_out
import fr.skyle.escapy.resources.sign_out_guest_dialog_message
import fr.skyle.escapy.resources.sign_out_guest_dialog_title
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel.HomeUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    uiState: HomeUIState,
    onSignOutClicked: () -> Unit,
    onGuestSignOutConfirmed: () -> Unit,
    onGuestSignOutDismissed: () -> Unit,
) {
    ESScreenStructure(style = ESScreenStructureDefaults.Style.STANDARD) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(ESTheme.spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            ESText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.home_coming_soon),
                style = ESTheme.typography.body,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            // Until the profile screen, which will hold it
            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.sign_out),
                onClick = onSignOutClicked,
                style = ESButtonDefaults.Style.SECONDARY,
                size = ESButtonDefaults.Size.LARGE,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))
        }
    }

    if (uiState.isGuestSignOutDialogShown) {
        ESDialog(
            title = stringResource(Res.string.sign_out_guest_dialog_title),
            message = stringResource(Res.string.sign_out_guest_dialog_message),
            confirmText = stringResource(Res.string.sign_out),
            onConfirm = onGuestSignOutConfirmed,
            dismissText = stringResource(Res.string.action_cancel),
            onDismiss = onGuestSignOutDismissed,
            style = ESDialogDefaults.Style.DESTRUCTIVE,
        )
    }
}

@Preview
@Composable
private fun PreviewHomeScreen() {
    ESTheme {
        HomeScreen(
            uiState = HomeUIState(),
            onSignOutClicked = {},
            onGuestSignOutConfirmed = {},
            onGuestSignOutDismissed = {},
        )
    }
}
