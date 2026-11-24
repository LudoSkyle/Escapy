package fr.skyle.escapy.designsystem.core.screenstructure

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarHost
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.theme.ESTheme

/**
 * The frame of a screen: its background under the system bars, a [topBar], a [bottomBar] fixed
 * under the content, the messages of [snackbarState]. The [content] receives the padding that keeps
 * it inside the bars, the system bars and the keyboard.
 */
@Composable
fun ESScreenStructure(
    style: ESScreenStructureDefaults.Style,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarState: ESSnackbarState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = {
            if (snackbarState != null) {
                ESSnackbarHost(state = snackbarState)
            }
        },
        containerColor = style.backgroundColor,
        contentColor = ESTheme.colors.textPrimary,
        contentWindowInsets = WindowInsets.safeDrawing,
        content = content,
    )
}

object ESScreenStructureDefaults {

    enum class Style {
        STANDARD;

        val backgroundColor: Color
            @Composable
            get() = when (this) {
                STANDARD -> ESTheme.colors.background
            }
    }
}
