package fr.skyle.escapy.designsystem.core.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/** The short messages of a screen, shown one after the other at its bottom. */
@Stable
class ESSnackbarState internal constructor(
    internal val hostState: SnackbarHostState,
) {

    /**
     * Shows a [message], with an action when [actionLabel] is given, and suspends until it goes
     * away. Returns whether the action was touched.
     */
    suspend fun show(message: String, actionLabel: String? = null): Boolean =
        hostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Long,
        ) == SnackbarResult.ActionPerformed
}

@Composable
fun rememberESSnackbarState(): ESSnackbarState =
    remember { ESSnackbarState(hostState = SnackbarHostState()) }
