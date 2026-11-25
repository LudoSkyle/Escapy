package fr.skyle.escapy.ui.core

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.runComposeUiTest
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.validation.PasswordRule
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.password_rule_digit
import fr.skyle.escapy.resources.password_rule_met
import fr.skyle.escapy.resources.password_rule_not_met
import fr.skyle.escapy.resources.password_rule_uppercase
import fr.skyle.escapy.test.ComposeTest
import fr.skyle.escapy.ui.core.password.PasswordRules
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class PasswordRulesTest : ComposeTest() {

    @Test
    fun `each rule tells a screen reader whether it is met`() = runComposeUiTest {
        // Given
        var metRule = ""
        var missingRule = ""
        setContent {
            metRule = "${stringResource(Res.string.password_rule_uppercase)}, ${stringResource(Res.string.password_rule_met)}"
            missingRule = "${stringResource(Res.string.password_rule_digit)}, ${stringResource(Res.string.password_rule_not_met)}"
            ESTheme {
                PasswordRules(missingRules = setOf(PasswordRule.DIGIT))
            }
        }

        // Then
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(metRule))).assertExists()
        onNode(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(missingRule))).assertExists()
    }
}
