package fr.skyle.escapy.ui.core.password

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.icon.ESIcon
import fr.skyle.escapy.designsystem.core.icon.ESIconDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.validation.PasswordPolicy
import fr.skyle.escapy.domain.validation.PasswordRule
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.ic_check_circle
import fr.skyle.escapy.resources.ic_radio_button_unchecked
import fr.skyle.escapy.resources.password_rule_digit
import fr.skyle.escapy.resources.password_rule_met
import fr.skyle.escapy.resources.password_rule_min_length
import fr.skyle.escapy.resources.password_rule_not_met
import fr.skyle.escapy.resources.password_rule_special_character
import fr.skyle.escapy.resources.password_rule_uppercase
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The rules of a new password, each met or not: by an icon and a color, and by words for a screen
 * reader, never by the color alone.
 */
@Composable
fun PasswordRules(
    missingRules: Set<PasswordRule>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        PasswordRule.entries.forEach { rule ->
            val isMet = rule !in missingRules
            val label = rule.label()
            val state = stringResource(if (isMet) Res.string.password_rule_met else Res.string.password_rule_not_met)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = ESTheme.spacing.xxs)
                    .clearAndSetSemantics { contentDescription = "$label, $state" },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ESIcon(
                    painter = painterResource(if (isMet) Res.drawable.ic_check_circle else Res.drawable.ic_radio_button_unchecked),
                    contentDescription = null,
                    size = ESIconDefaults.Size.SMALL,
                    tint = if (isMet) ESTheme.colors.success else ESTheme.colors.textSecondary,
                )

                Spacer(modifier = Modifier.width(ESTheme.spacing.xs))

                ESText(
                    modifier = Modifier.weight(1f),
                    text = label,
                    style = ESTheme.typography.bodySmall,
                    color = if (isMet) ESTheme.colors.textPrimary else ESTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun PasswordRule.label(): String =
    when (this) {
        PasswordRule.MIN_LENGTH -> stringResource(Res.string.password_rule_min_length, PasswordPolicy.MIN_LENGTH)
        PasswordRule.UPPERCASE -> stringResource(Res.string.password_rule_uppercase)
        PasswordRule.DIGIT -> stringResource(Res.string.password_rule_digit)
        PasswordRule.SPECIAL_CHARACTER -> stringResource(Res.string.password_rule_special_character)
    }

@Preview
@Composable
private fun PreviewPasswordRules() {
    ESTheme {
        PasswordRules(missingRules = setOf(PasswordRule.DIGIT, PasswordRule.SPECIAL_CHARACTER))
    }
}
