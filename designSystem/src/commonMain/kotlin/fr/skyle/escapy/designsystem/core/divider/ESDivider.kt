package fr.skyle.escapy.designsystem.core.divider

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.theme.ESTheme

/** A thin line between two rows, decorative. */
@Composable
fun ESDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = ESTheme.colors.outlineVariant,
    )
}
