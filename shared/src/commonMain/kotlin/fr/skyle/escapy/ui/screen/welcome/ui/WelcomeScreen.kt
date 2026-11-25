package fr.skyle.escapy.ui.screen.welcome.ui

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
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.app_name
import fr.skyle.escapy.resources.logo_keyhole
import fr.skyle.escapy.resources.welcome_continue_as_guest
import fr.skyle.escapy.resources.welcome_sign_in
import fr.skyle.escapy.resources.welcome_sign_up
import fr.skyle.escapy.resources.welcome_tagline
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen(
    isSigningInAsGuest: Boolean,
    snackbarState: ESSnackbarState,
    onSignInClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
    onContinueAsGuestClicked: () -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        snackbarState = snackbarState,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(ESTheme.spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // The keyhole of the icon, a decoration: the name of the app follows
            Image(
                modifier = Modifier.size(160.dp),
                painter = painterResource(Res.drawable.logo_keyhole),
                contentDescription = null,
            )

            ESText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.app_name),
                style = ESTheme.typography.headline,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            ESText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.welcome_tagline),
                style = ESTheme.typography.body,
                color = ESTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.welcome_sign_in),
                onClick = onSignInClicked,
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.LARGE,
                isEnabled = !isSigningInAsGuest,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.s))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.welcome_sign_up),
                onClick = onSignUpClicked,
                style = ESButtonDefaults.Style.SECONDARY,
                size = ESButtonDefaults.Size.LARGE,
                isEnabled = !isSigningInAsGuest,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.welcome_continue_as_guest),
                onClick = onContinueAsGuestClicked,
                style = ESButtonDefaults.Style.TEXT,
                size = ESButtonDefaults.Size.MEDIUM,
                isLoading = isSigningInAsGuest,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewWelcomeScreen() {
    ESTheme {
        WelcomeScreen(
            isSigningInAsGuest = false,
            snackbarState = rememberESSnackbarState(),
            onSignInClicked = {},
            onSignUpClicked = {},
            onContinueAsGuestClicked = {},
        )
    }
}
