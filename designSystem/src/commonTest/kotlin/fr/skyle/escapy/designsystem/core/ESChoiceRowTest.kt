package fr.skyle.escapy.designsystem.core

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import fr.skyle.escapy.designsystem.core.choice.ESChoiceRow
import fr.skyle.escapy.designsystem.core.choice.ESChoiceRowDefaults
import fr.skyle.escapy.designsystem.test.ComposeTest
import fr.skyle.escapy.designsystem.theme.ESTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
internal class ESChoiceRowTest : ComposeTest() {

    @Test
    fun `a single choice is a selected radio button and the whole row takes the touch`() = runComposeUiTest {
        // Given
        var clicks = 0
        setContent {
            ESTheme {
                ESChoiceRow(label = "Au lancement", isSelected = true, onClick = { clicks++ }, kind = ESChoiceRowDefaults.Kind.SINGLE)
            }
        }

        // When
        onNodeWithText("Au lancement", useUnmergedTree = false).performClick()

        // Then
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)).assertIsSelected()
        assertEquals(expected = 1, actual = clicks)
    }

    @Test
    fun `a choice on its own is a check box`() = runComposeUiTest {
        // Given
        setContent {
            ESTheme {
                ESChoiceRow(label = "Mission finale", isSelected = false, onClick = {}, kind = ESChoiceRowDefaults.Kind.MULTIPLE)
            }
        }

        // Then
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ToggleableState))
            .assertIsOff()
    }
}
