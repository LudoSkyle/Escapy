package fr.skyle.escapy.ui.screen.forgotpassword.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.field_email
import fr.skyle.escapy.resources.forgot_password_back_to_sign_in
import fr.skyle.escapy.resources.forgot_password_explanation
import fr.skyle.escapy.resources.forgot_password_send
import fr.skyle.escapy.resources.forgot_password_sent
import fr.skyle.escapy.resources.forgot_password_title
import fr.skyle.escapy.ui.core.error.message
import fr.skyle.escapy.ui.screen.forgotpassword.ui.ForgotPasswordViewModel.ForgotPasswordUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ForgotPasswordScreen(
    email: String,
    uiState: ForgotPasswordUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.forgot_password_title),
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
        ) {
            val sentTo = uiState.sentTo
            if (sentTo != null) {
                // Whether the address has an account or not: the screen never tells
                ESText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.forgot_password_sent, sentTo),
                    style = ESTheme.typography.body,
                )

                Spacer(modifier = Modifier.height(ESTheme.spacing.l))

                ESButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.forgot_password_back_to_sign_in),
                    onClick = onBackClicked,
                    style = ESButtonDefaults.Style.PRIMARY,
                    size = ESButtonDefaults.Size.LARGE,
                )
            } else {
                ESText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.forgot_password_explanation),
                    style = ESTheme.typography.body,
                    color = ESTheme.colors.textSecondary,
                )

                Spacer(modifier = Modifier.height(ESTheme.spacing.m))

                ESTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    onValueChange = onEmailChanged,
                    label = stringResource(Res.string.field_email),
                    kind = ESTextFieldDefaults.Kind.EMAIL,
                    style = ESTextFieldDefaults.Style.STANDARD,
                    isEnabled = !uiState.isSending,
                    errorMessage = uiState.emailError?.message(),
                    imeAction = ImeAction.Send,
                    onImeAction = onSendClicked,
                )

                Spacer(modifier = Modifier.height(ESTheme.spacing.l))

                ESButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.forgot_password_send),
                    onClick = onSendClicked,
                    style = ESButtonDefaults.Style.PRIMARY,
                    size = ESButtonDefaults.Size.LARGE,
                    isEnabled = email.isNotBlank() || uiState.isSending,
                    isLoading = uiState.isSending,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewForgotPasswordScreen() {
    ESTheme {
        ForgotPasswordScreen(
            email = "player@escapy.app",
            uiState = ForgotPasswordUIState(),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onEmailChanged = {},
            onSendClicked = {},
        )
    }
}

@Preview
@Composable
private fun PreviewForgotPasswordScreen_Sent() {
    ESTheme {
        ForgotPasswordScreen(
            email = "player@escapy.app",
            uiState = ForgotPasswordUIState(sentTo = "player@escapy.app"),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onEmailChanged = {},
            onSendClicked = {},
        )
    }
}
