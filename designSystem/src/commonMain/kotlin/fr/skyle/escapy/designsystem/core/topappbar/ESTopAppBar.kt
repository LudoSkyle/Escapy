package fr.skyle.escapy.designsystem.core.topappbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButton
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButtonDefaults
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.resources.Res
import fr.skyle.escapy.designsystem.resources.ic_arrow_back
import fr.skyle.escapy.designsystem.resources.navigate_back
import fr.skyle.escapy.designsystem.theme.ESTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The bar at the top of a screen: back when [onBackClick] is given, a [title], and [actions] at the
 * end. It grows with its title rather than cutting it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ESTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        title = {
            ESText(
                text = title,
                style = ESTheme.typography.subtitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                ESIconButton(
                    icon = painterResource(Res.drawable.ic_arrow_back),
                    contentDescription = stringResource(Res.string.navigate_back),
                    onClick = onBackClick,
                    style = ESIconButtonDefaults.Style.STANDARD,
                    size = ESIconButtonDefaults.Size.MEDIUM,
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ESTheme.colors.background,
            scrolledContainerColor = ESTheme.colors.surface,
            titleContentColor = ESTheme.colors.textPrimary,
            navigationIconContentColor = ESTheme.colors.textPrimary,
            actionIconContentColor = ESTheme.colors.textPrimary,
        ),
    )
}

@Preview
@Composable
private fun PreviewESTopAppBar() {
    ESTheme {
        ESTopAppBar(
            title = "Profile",
            onBackClick = {},
        )
    }
}
