package fr.skyle.escapy.designsystem.core

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.test.ComposeTest
import fr.skyle.escapy.designsystem.theme.ESTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ESSnackbarTest : ComposeTest() {

    @Test
    fun `a message shows at the bottom of the screen with its action`() = runComposeUiTest {
        // Given
        setContent {
            ESTheme {
                val snackbarState = rememberESSnackbarState()
                LaunchedEffect(Unit) {
                    snackbarState.show(
                        message = "Avatar saved",
                        actionLabel = "Undo",
                    )
                }
                ESScreenStructure(
                    style = ESScreenStructureDefaults.Style.STANDARD,
                    snackbarState = snackbarState,
                ) {}
            }
        }

        // Then
        onNodeWithText("Avatar saved").assertExists()
        onNodeWithText("Undo").assertExists()
    }
}
