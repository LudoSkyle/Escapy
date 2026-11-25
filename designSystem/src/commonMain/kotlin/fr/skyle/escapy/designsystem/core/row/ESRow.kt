package fr.skyle.escapy.designsystem.core.row

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.icon.ESIcon
import fr.skyle.escapy.designsystem.core.icon.ESIconDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.resources.Res
import fr.skyle.escapy.designsystem.resources.ic_chevron_right
import fr.skyle.escapy.designsystem.theme.ESTheme
import org.jetbrains.compose.resources.painterResource

/**
 * A row of a list, in a card, that opens something: a [label], an optional [supportingText] under
 * it, and a chevron. The whole row is the target of the touch, and reads as one button.
 */
@Composable
fun ESRow(
    label: String,
    onClick: () -> Unit,
    style: ESRowDefaults.Style,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = ESTheme.spacing.m, vertical = ESTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            ESText(
                text = label,
                style = ESTheme.typography.body,
                color = style.labelColor,
            )

            if (supportingText != null) {
                ESText(
                    text = supportingText,
                    style = ESTheme.typography.bodySmall,
                    color = ESTheme.colors.textSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.width(ESTheme.spacing.xs))

        ESIcon(
            painter = painterResource(Res.drawable.ic_chevron_right),
            contentDescription = null,
            size = ESIconDefaults.Size.MEDIUM,
            tint = ESTheme.colors.textSecondary,
        )
    }
}

object ESRowDefaults {

    enum class Style {
        STANDARD,
        // An action that deletes or loses something
        DESTRUCTIVE;

        val labelColor: Color
            @Composable
            get() = when (this) {
                STANDARD -> ESTheme.colors.textPrimary
                DESTRUCTIVE -> ESTheme.colors.error
            }
    }
}

@Preview
@Composable
private fun PreviewESRow() {
    ESTheme {
        ESCard(
            modifier = Modifier.padding(ESTheme.spacing.m),
            style = ESCardDefaults.Style.STANDARD,
        ) {
            ESRow(
                label = "Change the email",
                supportingText = "player@escapy.app",
                onClick = {},
                style = ESRowDefaults.Style.STANDARD,
            )
            ESRow(
                label = "Delete the account",
                onClick = {},
                style = ESRowDefaults.Style.DESTRUCTIVE,
            )
        }
    }
}
