package fr.skyle.escapy.designsystem.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.test.ComposeTest
import fr.skyle.escapy.designsystem.theme.ESTheme
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
internal class ESTextFieldTest : ComposeTest() {

    @Test
    fun `a field tells a screen reader its error`() = runComposeUiTest {
        // Given
        setContent {
            ESTheme {
                ESTextField(
                    value = "a",
                    onValueChange = {},
                    label = "Username",
                    kind = ESTextFieldDefaults.Kind.USERNAME,
                    style = ESTextFieldDefaults.Style.STANDARD,
                    errorMessage = "Too short",
                )
            }
        }

        // Then
        onNode(hasSetTextAction()).assert(SemanticsMatcher.expectValue(SemanticsProperties.Error, "Too short"))
    }

    @Test
    fun `a password field offers to show and then to hide its password`() = runComposeUiTest {
        // Given
        setContent {
            ESTheme {
                ESTextField(
                    value = "Escapy-2026",
                    onValueChange = {},
                    label = "Password",
                    kind = ESTextFieldDefaults.Kind.PASSWORD,
                    style = ESTextFieldDefaults.Style.STANDARD,
                )
            }
        }
        val eye = hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription) and !hasSetTextAction()
        onNode(hasSetTextAction()).assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
        val descriptionBefore = onNode(eye).fetchSemanticsNode().config[SemanticsProperties.ContentDescription]

        // When
        onNode(eye).performClick()

        // Then: the eye now offers to hide it. The field stays a password field for the system, shown or
        // not, as its keyboard is a password keyboard
        val descriptionAfter = onNode(eye).fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        assertNotEquals(
            illegal = descriptionBefore,
            actual = descriptionAfter,
        )
    }

    @Test
    fun `a message field grows with its lines`() = runComposeUiTest {
        // Given
        var text by mutableStateOf("Une ligne")
        setContent {
            ESTheme {
                ESTextField(
                    value = text,
                    onValueChange = { value -> text = value },
                    label = "Message",
                    kind = ESTextFieldDefaults.Kind.MESSAGE,
                    style = ESTextFieldDefaults.Style.STANDARD,
                )
            }
        }
        val oneLine = onNode(hasSetTextAction()).fetchSemanticsNode().size.height

        // When
        text = "Une ligne\nDeux lignes\nTrois lignes"
        waitForIdle()

        // Then
        assertTrue(onNode(hasSetTextAction()).fetchSemanticsNode().size.height > oneLine)
    }
}
