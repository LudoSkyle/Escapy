package fr.skyle.escapy.designsystem.core

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.test.ComposeTest
import fr.skyle.escapy.designsystem.theme.ESTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
internal class ESTopAppBarTest : ComposeTest() {

    @Test
    fun `the back button is named for a screen reader and goes back`() = runComposeUiTest {
        // Given
        var backs = 0
        setContent {
            ESTheme {
                ESTopAppBar(
                    title = "Profile",
                    onBackClick = { backs++ },
                )
            }
        }
        val back = hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription)

        // When
        val description = onNode(back).fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        onNode(back).performClick()

        // Then
        assertTrue(actual = description.single().isNotBlank())
        assertEquals(
            expected = 1,
            actual = backs,
        )
    }
}
