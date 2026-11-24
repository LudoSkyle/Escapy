package fr.skyle.escapy.designsystem.core.snackbar

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme

@Composable
internal fun ESSnackbarHost(
    state: ESSnackbarState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        modifier = modifier,
        hostState = state.hostState,
    ) { data ->
        Snackbar(
            shape = ESTheme.shapes.small,
            containerColor = ESTheme.colors.surfaceHighest,
            contentColor = ESTheme.colors.textPrimary,
            action = data.visuals.actionLabel?.let { label ->
                {
                    TextButton(onClick = data::performAction) {
                        ESText(
                            text = label,
                            style = ESTheme.typography.label,
                            color = ESTheme.colors.primary,
                        )
                    }
                }
            },
        ) {
            ESText(
                text = data.visuals.message,
                style = ESTheme.typography.bodySmall,
            )
        }
    }
}
