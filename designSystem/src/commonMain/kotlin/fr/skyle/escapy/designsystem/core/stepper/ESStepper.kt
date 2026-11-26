package fr.skyle.escapy.designsystem.core.stepper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButton
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButtonDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.resources.Res
import fr.skyle.escapy.designsystem.resources.ic_add
import fr.skyle.escapy.designsystem.resources.ic_remove
import fr.skyle.escapy.designsystem.theme.ESTheme
import org.jetbrains.compose.resources.painterResource

/**
 * A value chosen step by step, among a few: a [label], the [value] as the screen writes it, and a
 * button on each side to go down or up. A button is disabled at its end of the range. The value is
 * read again by the screen reader each time it changes.
 */
@Composable
fun ESStepper(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseContentDescription: String,
    increaseContentDescription: String,
    style: ESStepperDefaults.Style,
    size: ESStepperDefaults.Size,
    modifier: Modifier = Modifier,
    isDecreaseEnabled: Boolean = true,
    isIncreaseEnabled: Boolean = true,
    supportingText: String? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = size.minHeight)
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

        ESIconButton(
            icon = painterResource(Res.drawable.ic_remove),
            contentDescription = decreaseContentDescription,
            onClick = onDecrease,
            style = ESIconButtonDefaults.Style.FILLED,
            size = ESIconButtonDefaults.Size.MEDIUM,
            isEnabled = isDecreaseEnabled,
        )

        ESText(
            modifier = Modifier
                .widthIn(min = size.valueMinWidth)
                .padding(horizontal = ESTheme.spacing.xs)
                .semantics { liveRegion = LiveRegionMode.Polite },
            text = value,
            style = ESTheme.typography.subtitle,
            color = style.valueColor,
            textAlign = TextAlign.Center,
        )

        ESIconButton(
            icon = painterResource(Res.drawable.ic_add),
            contentDescription = increaseContentDescription,
            onClick = onIncrease,
            style = ESIconButtonDefaults.Style.FILLED,
            size = ESIconButtonDefaults.Size.MEDIUM,
            isEnabled = isIncreaseEnabled,
        )
    }
}

object ESStepperDefaults {

    enum class Style {
        STANDARD;

        val labelColor: Color
            @Composable
            get() = when (this) {
                STANDARD -> ESTheme.colors.textPrimary
            }

        val valueColor: Color
            @Composable
            get() = when (this) {
                STANDARD -> ESTheme.colors.textPrimary
            }
    }

    enum class Size {
        MEDIUM;

        val minHeight: Dp
            get() = when (this) {
                MEDIUM -> 64.dp
            }

        // Wide enough for "2 h 55" or "No limit": the buttons do not move as the value changes
        val valueMinWidth: Dp
            get() = when (this) {
                MEDIUM -> 88.dp
            }
    }
}

@Preview
@Composable
private fun PreviewESStepper() {
    ESTheme {
        ESCard(
            modifier = Modifier.padding(ESTheme.spacing.m),
            style = ESCardDefaults.Style.STANDARD,
        ) {
            ESStepper(
                label = "Duration",
                value = "1 h 30",
                onDecrease = {},
                onIncrease = {},
                decreaseContentDescription = "Shorter",
                increaseContentDescription = "Longer",
                style = ESStepperDefaults.Style.STANDARD,
                size = ESStepperDefaults.Size.MEDIUM,
            )
            ESStepper(
                label = "Players",
                supportingText = "The game master apart",
                value = "No limit",
                onDecrease = {},
                onIncrease = {},
                decreaseContentDescription = "Fewer",
                increaseContentDescription = "More",
                style = ESStepperDefaults.Style.STANDARD,
                size = ESStepperDefaults.Size.MEDIUM,
                isDecreaseEnabled = false,
            )
        }
    }
}
