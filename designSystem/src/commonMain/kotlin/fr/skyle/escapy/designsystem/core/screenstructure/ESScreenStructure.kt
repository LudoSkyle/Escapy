package fr.skyle.escapy.designsystem.core.screenstructure

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
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
    val focusManager = LocalFocusManager.current

    Scaffold(
        // A touch out of the fields and the buttons ends the typing: iOS keeps the keyboard otherwise,
        // over the buttons under it. A field or a button handles its own touch first
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures {
                focusManager.clearFocus()
            }
        },
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
