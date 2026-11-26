package fr.skyle.escapy.ui.core

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.test.ComposeTest
import fr.skyle.escapy.ui.core.game.durationText
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class GameTextsTest : ComposeTest() {

    // Compose Resources leave a format they do not know as it is, such as a width: "1 h %2$02d"
    @Test
    fun `a duration reads as people say it with the minutes on two digits`() = runComposeUiTest {
        // Given
        setContent {
            ESTheme {
                Column {
                    listOf(45, 60, 65, 90, 180).forEach { minutes ->
                        ESText(text = durationText(minutes = minutes), style = ESTheme.typography.body)
                    }
                }
            }
        }

        // Then
        listOf("45 min", "1 h", "1 h 05", "1 h 30", "3 h").forEach { text ->
            onNodeWithText(text).assertExists()
        }
    }
}
