package fr.skyle.escapy.ui.screen.changeemail.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_back_to_profile
import fr.skyle.escapy.resources.change_email_action
import fr.skyle.escapy.resources.change_email_sent
import fr.skyle.escapy.resources.change_email_title
import fr.skyle.escapy.resources.field_current_password
import fr.skyle.escapy.resources.field_new_email
import fr.skyle.escapy.ui.core.error.currentPasswordMessage
import fr.skyle.escapy.ui.core.error.message
import fr.skyle.escapy.ui.core.form.AccountFormLayout
import fr.skyle.escapy.ui.screen.changeemail.ui.ChangeEmailViewModel.ChangeEmailUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChangeEmailScreen(
    currentPassword: String,
    newEmail: String,
    uiState: ChangeEmailUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewEmailChanged: (String) -> Unit,
    onChangeClicked: () -> Unit,
) {
    AccountFormLayout(
        title = stringResource(Res.string.change_email_title),
        snackbarState = snackbarState,
        onBackClicked = onBackClicked,
        result = uiState.sentTo?.let { sentTo -> stringResource(Res.string.change_email_sent, sentTo) },
        resultActionText = stringResource(Res.string.action_back_to_profile),
    ) {
        ESTextField(
            modifier = Modifier.fillMaxWidth(),
            value = currentPassword,
            onValueChange = onCurrentPasswordChanged,
            label = stringResource(Res.string.field_current_password),
            kind = ESTextFieldDefaults.Kind.PASSWORD,
            style = ESTextFieldDefaults.Style.STANDARD,
            isEnabled = !uiState.isChanging,
            errorMessage = uiState.passwordError?.currentPasswordMessage(),
            imeAction = ImeAction.Next,
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

        ESTextField(
            modifier = Modifier.fillMaxWidth(),
            value = newEmail,
            onValueChange = onNewEmailChanged,
            label = stringResource(Res.string.field_new_email),
            kind = ESTextFieldDefaults.Kind.EMAIL,
            style = ESTextFieldDefaults.Style.STANDARD,
            isEnabled = !uiState.isChanging,
            errorMessage = uiState.emailError?.message(),
            imeAction = ImeAction.Done,
            onImeAction = onChangeClicked,
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.l))

        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.change_email_action),
            onClick = onChangeClicked,
            style = ESButtonDefaults.Style.PRIMARY,
            size = ESButtonDefaults.Size.LARGE,
            isEnabled = (currentPassword.isNotEmpty() && newEmail.isNotBlank()) || uiState.isChanging,
            isLoading = uiState.isChanging,
        )
    }
}
