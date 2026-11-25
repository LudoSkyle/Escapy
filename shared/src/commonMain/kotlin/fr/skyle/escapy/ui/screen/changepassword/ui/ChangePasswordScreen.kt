package fr.skyle.escapy.ui.screen.changepassword.ui

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
import fr.skyle.escapy.domain.validation.PasswordPolicy
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_back_to_profile
import fr.skyle.escapy.resources.change_password_action
import fr.skyle.escapy.resources.change_password_done
import fr.skyle.escapy.resources.change_password_title
import fr.skyle.escapy.resources.field_current_password
import fr.skyle.escapy.resources.field_new_password
import fr.skyle.escapy.ui.core.error.currentPasswordMessage
import fr.skyle.escapy.ui.core.form.AccountFormLayout
import fr.skyle.escapy.ui.core.password.PasswordRules
import fr.skyle.escapy.ui.screen.changepassword.ui.ChangePasswordViewModel.ChangePasswordUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChangePasswordScreen(
    currentPassword: String,
    newPassword: String,
    uiState: ChangePasswordUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onCurrentPasswordChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onChangeClicked: () -> Unit,
) {
    AccountFormLayout(
        title = stringResource(Res.string.change_password_title),
        snackbarState = snackbarState,
        onBackClicked = onBackClicked,
        result = if (uiState.isChanged) stringResource(Res.string.change_password_done) else null,
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
            errorMessage = uiState.currentPasswordError?.currentPasswordMessage(),
            imeAction = ImeAction.Next,
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

        ESTextField(
            modifier = Modifier.fillMaxWidth(),
            value = newPassword,
            onValueChange = onNewPasswordChanged,
            label = stringResource(Res.string.field_new_password),
            kind = ESTextFieldDefaults.Kind.NEW_PASSWORD,
            style = ESTextFieldDefaults.Style.STANDARD,
            isEnabled = !uiState.isChanging,
            imeAction = ImeAction.Done,
            onImeAction = onChangeClicked,
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.s))

        PasswordRules(
            modifier = Modifier.fillMaxWidth(),
            missingRules = PasswordPolicy.missingRules(newPassword),
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.l))

        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.change_password_action),
            onClick = onChangeClicked,
            style = ESButtonDefaults.Style.PRIMARY,
            size = ESButtonDefaults.Size.LARGE,
            isEnabled = ChangePasswordViewModel.canChange(currentPassword = currentPassword, newPassword = newPassword) || uiState.isChanging,
            isLoading = uiState.isChanging,
        )
    }
}
