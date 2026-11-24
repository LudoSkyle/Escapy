package fr.skyle.escapy.designsystem.core.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme

/**
 * A question before an action: a [title], a [message], the action and a way out. A [style]
 * DESTRUCTIVE draws the action in the color of what cannot be undone.
 */
@Composable
fun ESDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismiss: () -> Unit,
    style: ESDialogDefaults.Style,
    modifier: Modifier = Modifier,
    isConfirmLoading: Boolean = false,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            if (!isConfirmLoading) {
                onDismiss()
            }
        },
        title = {
            ESText(
                text = title,
                style = ESTheme.typography.title,
            )
        },
        text = {
            ESText(
                text = message,
                style = ESTheme.typography.body,
            )
        },
        confirmButton = {
            ESButton(
                text = confirmText,
                onClick = onConfirm,
                style = style.confirmButtonStyle,
                size = ESButtonDefaults.Size.MEDIUM,
                isLoading = isConfirmLoading,
            )
        },
        dismissButton = {
            ESButton(
                text = dismissText,
                onClick = onDismiss,
                style = ESButtonDefaults.Style.TEXT,
                size = ESButtonDefaults.Size.MEDIUM,
                isEnabled = !isConfirmLoading,
            )
        },
        shape = ESTheme.shapes.large,
        containerColor = ESTheme.colors.surface,
        titleContentColor = ESTheme.colors.textPrimary,
        textContentColor = ESTheme.colors.textSecondary,
    )
}

object ESDialogDefaults {

    enum class Style {
        STANDARD,
        DESTRUCTIVE;

        internal val confirmButtonStyle: ESButtonDefaults.Style
            get() = when (this) {
                STANDARD -> ESButtonDefaults.Style.PRIMARY
                DESTRUCTIVE -> ESButtonDefaults.Style.DESTRUCTIVE
            }
    }
}

@Preview
@Composable
private fun PreviewESDialog() {
    ESTheme {
        ESDialog(
            title = "Delete the account?",
            message = "Your profile and your games are deleted. This cannot be undone.",
            confirmText = "Delete",
            onConfirm = {},
            dismissText = "Cancel",
            onDismiss = {},
            style = ESDialogDefaults.Style.DESTRUCTIVE,
        )
    }
}
