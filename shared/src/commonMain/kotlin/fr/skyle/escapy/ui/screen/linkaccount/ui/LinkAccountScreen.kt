package fr.skyle.escapy.ui.screen.linkaccount.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.validation.PasswordPolicy
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_back_to_profile
import fr.skyle.escapy.resources.field_email
import fr.skyle.escapy.resources.field_new_password
import fr.skyle.escapy.resources.link_account_action
import fr.skyle.escapy.resources.link_account_done
import fr.skyle.escapy.resources.link_account_explanation
import fr.skyle.escapy.resources.link_account_title
import fr.skyle.escapy.ui.core.error.message
import fr.skyle.escapy.ui.core.form.AccountFormLayout
import fr.skyle.escapy.ui.core.password.PasswordRules
import fr.skyle.escapy.ui.screen.linkaccount.ui.LinkAccountViewModel.LinkAccountUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun LinkAccountScreen(
    email: String,
    password: String,
    uiState: LinkAccountUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLinkClicked: () -> Unit,
) {
    AccountFormLayout(
        title = stringResource(Res.string.link_account_title),
        snackbarState = snackbarState,
        onBackClicked = onBackClicked,
        result = uiState.linkedTo?.let { linkedTo -> stringResource(Res.string.link_account_done, linkedTo) },
        resultActionText = stringResource(Res.string.action_back_to_profile),
    ) {
        ESText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.link_account_explanation),
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
            isEnabled = !uiState.isLinking,
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
            isEnabled = !uiState.isLinking,
            imeAction = ImeAction.Done,
            onImeAction = onLinkClicked,
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.s))

        PasswordRules(
            modifier = Modifier.fillMaxWidth(),
            missingRules = PasswordPolicy.missingRules(password),
        )

        Spacer(modifier = Modifier.height(ESTheme.spacing.l))

        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.link_account_action),
            onClick = onLinkClicked,
            style = ESButtonDefaults.Style.PRIMARY,
            size = ESButtonDefaults.Size.LARGE,
            isEnabled = LinkAccountViewModel.canLink(email = email, password = password) || uiState.isLinking,
            isLoading = uiState.isLinking,
        )
    }
}
