package fr.skyle.escapy.ui.screen.deleteaccount.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.dialog.ESDialog
import fr.skyle.escapy.designsystem.core.dialog.ESDialogDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_cancel
import fr.skyle.escapy.resources.delete_account_action
import fr.skyle.escapy.resources.delete_account_confirmation_message
import fr.skyle.escapy.resources.delete_account_confirmation_title
import fr.skyle.escapy.resources.delete_account_title
import fr.skyle.escapy.resources.delete_account_google_hint
import fr.skyle.escapy.resources.delete_account_warning
import fr.skyle.escapy.resources.field_current_password
import fr.skyle.escapy.ui.core.error.currentPasswordMessage
import fr.skyle.escapy.ui.core.form.AccountFormLayout
import fr.skyle.escapy.ui.screen.deleteaccount.ui.DeleteAccountViewModel.DeleteAccountUIState
import fr.skyle.escapy.ui.screen.deleteaccount.ui.DeleteAccountViewModel.DeletionMethod
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteAccountScreen(
    currentPassword: String,
    uiState: DeleteAccountUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onCurrentPasswordChanged: (String) -> Unit,
    onDeleteClicked: () -> Unit,
    onDeletionConfirmed: () -> Unit,
    onConfirmationDismissed: () -> Unit,
) {
    AccountFormLayout(
        title = stringResource(Res.string.delete_account_title),
        snackbarState = snackbarState,
        onBackClicked = onBackClicked,
        // Once deleted, the session ends and the app leaves this screen
        result = null,
        resultActionText = "",
    ) {
        ESText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.delete_account_warning),
            style = ESTheme.typography.body,
        )

        when (uiState.method) {
            DeletionMethod.GUEST -> Unit
            DeletionMethod.GOOGLE -> {
                Spacer(modifier = Modifier.height(ESTheme.spacing.s))

                ESText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.delete_account_google_hint),
                    style = ESTheme.typography.body,
                    color = ESTheme.colors.textSecondary,
                )
            }
            DeletionMethod.PASSWORD -> {
                Spacer(modifier = Modifier.height(ESTheme.spacing.m))

                ESTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChanged,
                    label = stringResource(Res.string.field_current_password),
                    kind = ESTextFieldDefaults.Kind.PASSWORD,
                    style = ESTextFieldDefaults.Style.STANDARD,
                    isEnabled = !uiState.isDeleting,
                    errorMessage = uiState.passwordError?.currentPasswordMessage(),
                    imeAction = ImeAction.Done,
                    onImeAction = onDeleteClicked,
                )
            }
        }

        Spacer(modifier = Modifier.height(ESTheme.spacing.l))

        ESButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.delete_account_action),
            onClick = onDeleteClicked,
            style = ESButtonDefaults.Style.DESTRUCTIVE,
            size = ESButtonDefaults.Size.LARGE,
            isEnabled = DeleteAccountViewModel.canDelete(method = uiState.method, currentPassword = currentPassword),
        )
    }

    if (uiState.isConfirmationShown) {
        ESDialog(
            title = stringResource(Res.string.delete_account_confirmation_title),
            message = stringResource(Res.string.delete_account_confirmation_message),
            confirmText = stringResource(Res.string.delete_account_action),
            onConfirm = onDeletionConfirmed,
            dismissText = stringResource(Res.string.action_cancel),
            onDismiss = onConfirmationDismissed,
            style = ESDialogDefaults.Style.DESTRUCTIVE,
            isConfirmLoading = uiState.isDeleting,
        )
    }
}
