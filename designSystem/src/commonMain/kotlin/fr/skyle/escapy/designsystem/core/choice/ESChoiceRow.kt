package fr.skyle.escapy.designsystem.core.choice

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme

/**
 * A choice on a row: one among others ([ESChoiceRowDefaults.Kind.SINGLE], a radio button, in a
 * group the screen marks with selectableGroup), or a choice on its own ([ESChoiceRowDefaults.Kind.MULTIPLE],
 * a check box). The whole row is the target, read by a screen reader as a radio button or a check box.
 */
@Composable
fun ESChoiceRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    kind: ESChoiceRowDefaults.Kind,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isEnabled: Boolean = true,
) {
    val target = when (kind) {
        ESChoiceRowDefaults.Kind.SINGLE -> Modifier.selectable(
            selected = isSelected,
            enabled = isEnabled,
            role = Role.RadioButton,
            onClick = onClick,
        )
        ESChoiceRowDefaults.Kind.MULTIPLE -> Modifier.toggleable(
            value = isSelected,
            enabled = isEnabled,
            role = Role.Checkbox,
            onValueChange = { onClick() },
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .then(target)
            .padding(horizontal = ESTheme.spacing.m, vertical = ESTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The row handles the touch: the control only shows the state
        when (kind) {
            ESChoiceRowDefaults.Kind.SINGLE -> RadioButton(
                selected = isSelected,
                onClick = null,
                enabled = isEnabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = ESTheme.colors.primary,
                    unselectedColor = ESTheme.colors.outline,
                    disabledSelectedColor = ESTheme.colors.disabledContent,
                    disabledUnselectedColor = ESTheme.colors.disabledContent,
                ),
            )
            ESChoiceRowDefaults.Kind.MULTIPLE -> Checkbox(
                checked = isSelected,
                onCheckedChange = null,
                enabled = isEnabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = ESTheme.colors.primary,
                    uncheckedColor = ESTheme.colors.outline,
                    checkmarkColor = ESTheme.colors.onPrimary,
                    disabledCheckedColor = ESTheme.colors.disabledContent,
                    disabledUncheckedColor = ESTheme.colors.disabledContent,
                ),
            )
        }

        Spacer(modifier = Modifier.width(ESTheme.spacing.s))

        Column(modifier = Modifier.weight(1f)) {
            ESText(
                text = label,
                style = ESTheme.typography.body,
                color = if (isEnabled) ESTheme.colors.textPrimary else ESTheme.colors.disabledContent,
            )
            if (supportingText != null) {
                ESText(
                    text = supportingText,
                    style = ESTheme.typography.bodySmall,
                    color = ESTheme.colors.textSecondary,
                )
            }
        }
    }
}

object ESChoiceRowDefaults {

    enum class Kind {
        // One among others: a radio button
        SINGLE,

        // A choice on its own: a check box
        MULTIPLE,
    }
}

@Preview
@Composable
private fun PreviewESChoiceRow() {
    ESTheme {
        ESCard(
            modifier = Modifier.padding(ESTheme.spacing.m),
            style = ESCardDefaults.Style.STANDARD,
        ) {
            ESChoiceRow(label = "Au lancement", isSelected = true, onClick = {}, kind = ESChoiceRowDefaults.Kind.SINGLE)
            ESChoiceRow(
                label = "Après une autre mission",
                supportingText = "Le coffre",
                isSelected = false,
                onClick = {},
                kind = ESChoiceRowDefaults.Kind.SINGLE,
            )
            ESChoiceRow(label = "Mission finale", isSelected = true, onClick = {}, kind = ESChoiceRowDefaults.Kind.MULTIPLE)
            ESChoiceRow(label = "Désactivé", isSelected = false, onClick = {}, kind = ESChoiceRowDefaults.Kind.MULTIPLE, isEnabled = false)
        }
    }
}
