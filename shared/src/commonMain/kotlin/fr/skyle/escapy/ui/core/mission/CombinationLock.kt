package fr.skyle.escapy.ui.core.mission

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButton
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButtonDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.code_lock_digit
import fr.skyle.escapy.resources.code_lock_next
import fr.skyle.escapy.resources.code_lock_previous
import fr.skyle.escapy.resources.ic_expand_less
import fr.skyle.escapy.resources.ic_expand_more
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * A combination lock of [length] wheels, for the code of a mission: each wheel turns from 0 to 9 and
 * round again, as on a padlock. [digits] always has [length] digits.
 */
@Composable
fun CombinationLock(
    digits: String,
    length: Int,
    onDigitsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val wheels = digits.padEnd(length, '0').take(length)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ESTheme.spacing.xxs, Alignment.CenterHorizontally),
    ) {
        wheels.forEachIndexed { index, digit ->
            val position = (index + 1).toString()
            val digitDescription = stringResource(Res.string.code_lock_digit, position, digit.toString())
            fun turn(by: Int) {
                val next = ((digit - '0' + by + 10) % 10).digitToChar()
                onDigitsChange(wheels.substring(0, index) + next + wheels.substring(index + 1))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ESIconButton(
                    icon = painterResource(Res.drawable.ic_expand_less),
                    contentDescription = stringResource(Res.string.code_lock_next, position),
                    onClick = { turn(1) },
                    style = ESIconButtonDefaults.Style.STANDARD,
                    size = ESIconButtonDefaults.Size.MEDIUM,
                    isEnabled = isEnabled,
                )
                ESText(
                    modifier = Modifier
                        .width(36.dp)
                        .border(width = 1.dp, color = ESTheme.colors.outline, shape = ESTheme.shapes.small)
                        .padding(vertical = ESTheme.spacing.xs)
                        .semantics { contentDescription = digitDescription },
                    text = digit.toString(),
                    style = ESTheme.typography.title,
                    color = ESTheme.colors.primary,
                    textAlign = TextAlign.Center,
                )
                ESIconButton(
                    icon = painterResource(Res.drawable.ic_expand_more),
                    contentDescription = stringResource(Res.string.code_lock_previous, position),
                    onClick = { turn(-1) },
                    style = ESIconButtonDefaults.Style.STANDARD,
                    size = ESIconButtonDefaults.Size.MEDIUM,
                    isEnabled = isEnabled,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCombinationLock() {
    ESTheme {
        CombinationLock(digits = "0427", length = 4, onDigitsChange = {})
    }
}
