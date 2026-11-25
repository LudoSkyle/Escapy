package fr.skyle.escapy.ui.screen.signin.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.field_email
import fr.skyle.escapy.resources.field_password
import fr.skyle.escapy.resources.sign_in_action
import fr.skyle.escapy.resources.sign_in_forgot_password
import fr.skyle.escapy.resources.sign_in_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SignInScreen(
    email: String,
    password: String,
    isSigningIn: Boolean,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignInClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.sign_in_title),
                onBackClick = onBackClicked,
            )
        },
        snackbarState = snackbarState,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(ESTheme.spacing.l),
            horizontalAlignment = Alignment.End,
        ) {
            ESTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = onEmailChanged,
                label = stringResource(Res.string.field_email),
                kind = ESTextFieldDefaults.Kind.EMAIL,
                style = ESTextFieldDefaults.Style.STANDARD,
                isEnabled = !isSigningIn,
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            ESTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = onPasswordChanged,
                label = stringResource(Res.string.field_password),
                kind = ESTextFieldDefaults.Kind.PASSWORD,
                style = ESTextFieldDefaults.Style.STANDARD,
                isEnabled = !isSigningIn,
                imeAction = ImeAction.Done,
                onImeAction = onSignInClicked,
            )

            ESButton(
                modifier = Modifier.padding(top = ESTheme.spacing.xxs),
                text = stringResource(Res.string.sign_in_forgot_password),
                onClick = onForgotPasswordClicked,
                style = ESButtonDefaults.Style.TEXT,
                size = ESButtonDefaults.Size.MEDIUM,
                isEnabled = !isSigningIn,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.sign_in_action),
                onClick = onSignInClicked,
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.LARGE,
                isEnabled = SignInViewModel.canSignIn(email = email, password = password) || isSigningIn,
                isLoading = isSigningIn,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSignInScreen() {
    ESTheme {
        SignInScreen(
            email = "player@escapy.app",
            password = "Escapy-2026",
            isSigningIn = false,
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onSignInClicked = {},
            onForgotPasswordClicked = {},
        )
    }
}
