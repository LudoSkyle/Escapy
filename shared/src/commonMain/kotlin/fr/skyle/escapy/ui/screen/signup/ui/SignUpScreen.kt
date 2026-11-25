package fr.skyle.escapy.ui.screen.signup.ui

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
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.validation.PasswordPolicy
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.field_email
import fr.skyle.escapy.resources.field_new_password
import fr.skyle.escapy.resources.sign_up_action
import fr.skyle.escapy.resources.sign_up_title
import fr.skyle.escapy.ui.core.error.message
import fr.skyle.escapy.ui.core.password.PasswordRules
import fr.skyle.escapy.ui.screen.signup.ui.SignUpViewModel.SignUpUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun SignUpScreen(
    email: String,
    password: String,
    uiState: SignUpUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignUpClicked: () -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.sign_up_title),
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
            ESTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = onEmailChanged,
                label = stringResource(Res.string.field_email),
                kind = ESTextFieldDefaults.Kind.EMAIL,
                style = ESTextFieldDefaults.Style.STANDARD,
                isEnabled = !uiState.isSigningUp,
                errorMessage = uiState.emailError?.message(),
                imeAction = ImeAction.Next,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            ESTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = onPasswordChanged,
                label = stringResource(Res.string.field_new_password),
                kind = ESTextFieldDefaults.Kind.NEW_PASSWORD,
                style = ESTextFieldDefaults.Style.STANDARD,
                isEnabled = !uiState.isSigningUp,
                imeAction = ImeAction.Done,
                onImeAction = onSignUpClicked,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.s))

            PasswordRules(
                modifier = Modifier.fillMaxWidth(),
                missingRules = PasswordPolicy.missingRules(password),
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.sign_up_action),
                onClick = onSignUpClicked,
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.LARGE,
                isEnabled = SignUpViewModel.canSignUp(email = email, password = password) || uiState.isSigningUp,
                isLoading = uiState.isSigningUp,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewSignUpScreen() {
    ESTheme {
        SignUpScreen(
            email = "player@escapy.app",
            password = "Escapy",
            uiState = SignUpUIState(),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onSignUpClicked = {},
        )
    }
}
