package fr.skyle.escapy.designsystem.core

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.test.ComposeTest
import fr.skyle.escapy.designsystem.theme.ESTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class ESButtonTest : ComposeTest() {

    @Test
    fun `a button calls its action when touched`() = runComposeUiTest {
        // Given
        var clicks = 0
        setContent {
            ESTheme {
                ESButton(
                    text = "Join",
                    onClick = { clicks++ },
                    style = ESButtonDefaults.Style.PRIMARY,
                    size = ESButtonDefaults.Size.MEDIUM,
                )
            }
        }

        // When
        onNodeWithText("Join").performClick()

        // Then
        onNodeWithText("Join").assertIsEnabled()
        assertEquals(
            expected = 1,
            actual = clicks,
        )
    }

    @Test
    fun `a loading button reads as disabled and ignores a touch`() = runComposeUiTest {
        // Given
        var clicks = 0
        setContent {
            ESTheme {
                ESButton(
                    text = "Join",
                    onClick = { clicks++ },
                    style = ESButtonDefaults.Style.PRIMARY,
                    size = ESButtonDefaults.Size.MEDIUM,
                    isLoading = true,
                )
            }
        }

        // When
        onNodeWithText("Join", useUnmergedTree = true).performClick()

        // Then
        onNodeWithText("Join").assertIsNotEnabled()
        assertEquals(
            expected = 0,
            actual = clicks,
        )
    }
}
