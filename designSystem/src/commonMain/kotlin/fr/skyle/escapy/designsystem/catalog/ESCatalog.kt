package fr.skyle.escapy.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.dialog.ESDialog
import fr.skyle.escapy.designsystem.core.dialog.ESDialogDefaults
import fr.skyle.escapy.designsystem.core.divider.ESDivider
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButton
import fr.skyle.escapy.designsystem.core.iconbutton.ESIconButtonDefaults
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicator
import fr.skyle.escapy.designsystem.core.progress.ESProgressIndicatorDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.textfield.ESTextField
import fr.skyle.escapy.designsystem.core.textfield.ESTextFieldDefaults
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.resources.Res
import fr.skyle.escapy.designsystem.resources.ic_visibility
import fr.skyle.escapy.designsystem.theme.ESTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Every component of the design system on one screen, to look at them on a device. Opened by a
 * debug build only (see docs/UI/03_DESIGN_SYSTEM.md), never part of the navigation of the app.
 */
@Composable
fun ESCatalog() {
    val snackbarState = rememberESSnackbarState()
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("player@escapy.app") }
    var password by remember { mutableStateOf("Escapy-2026") }
    var isDialogShown by remember { mutableStateOf(false) }

    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = "Design system",
                onBackClick = {},
            )
        },
        snackbarState = snackbarState,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(ESTheme.spacing.m),
        ) {
            Swatches()

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESText(text = "Headline", style = ESTheme.typography.headline)
            ESText(text = "Title", style = ESTheme.typography.title)
            ESText(text = "Subtitle", style = ESTheme.typography.subtitle)
            ESText(text = "Body: the game master sends a hint.", style = ESTheme.typography.body)
            ESText(text = "Body small, secondary", style = ESTheme.typography.bodySmall, color = ESTheme.colors.textSecondary)
            ESText(text = "Caption, tertiary", style = ESTheme.typography.caption, color = ESTheme.colors.textTertiary)

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESButtonDefaults.Style.entries.forEach { style ->
                ESButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = style.name.lowercase().replaceFirstChar(Char::uppercase),
                    onClick = {
                        coroutineScope.launch {
                            snackbarState.show(message = "${style.name} touched", actionLabel = "Undo")
                        }
                    },
                    style = style,
                    size = ESButtonDefaults.Size.LARGE,
                )

                Spacer(modifier = Modifier.height(ESTheme.spacing.xs))
            }

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Loading",
                onClick = {},
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.MEDIUM,
                isLoading = true,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Disabled",
                onClick = {},
                style = ESButtonDefaults.Style.PRIMARY,
                size = ESButtonDefaults.Size.MEDIUM,
                isEnabled = false,
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.xs))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Open the dialog",
                onClick = { isDialogShown = true },
                style = ESButtonDefaults.Style.SECONDARY,
                size = ESButtonDefaults.Size.MEDIUM,
                leadingIcon = painterResource(Res.drawable.ic_visibility),
            )

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESCard(
                modifier = Modifier.fillMaxWidth(),
                style = ESCardDefaults.Style.STANDARD,
            ) {
                Column(modifier = Modifier.padding(ESTheme.spacing.m)) {
                    ESText(text = "Sign in", style = ESTheme.typography.subtitle)

                    Spacer(modifier = Modifier.height(ESTheme.spacing.s))

                    ESTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        kind = ESTextFieldDefaults.Kind.EMAIL,
                        style = ESTextFieldDefaults.Style.STANDARD,
                        imeAction = ImeAction.Next,
                    )

                    ESTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        kind = ESTextFieldDefaults.Kind.PASSWORD,
                        style = ESTextFieldDefaults.Style.STANDARD,
                        errorMessage = "Wrong password",
                        imeAction = ImeAction.Done,
                    )

                    ESTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = "",
                        onValueChange = {},
                        label = "Username",
                        kind = ESTextFieldDefaults.Kind.USERNAME,
                        style = ESTextFieldDefaults.Style.STANDARD,
                        supportingText = "From 3 to 20 characters",
                    )

                    Spacer(modifier = Modifier.height(ESTheme.spacing.s))

                    ESDivider()

                    Row(horizontalArrangement = Arrangement.spacedBy(ESTheme.spacing.xs)) {
                        ESIconButtonDefaults.Style.entries.forEach { style ->
                            ESIconButton(
                                icon = painterResource(Res.drawable.ic_visibility),
                                contentDescription = style.name,
                                onClick = {},
                                style = style,
                                size = ESIconButtonDefaults.Size.MEDIUM,
                            )
                        }

                        ESProgressIndicator(
                            modifier = Modifier.padding(ESTheme.spacing.xxs),
                            style = ESProgressIndicatorDefaults.Style.PRIMARY,
                            size = ESProgressIndicatorDefaults.Size.LARGE,
                        )
                    }
                }
            }
        }
    }

    if (isDialogShown) {
        ESDialog(
            title = "Delete the account?",
            message = "Your profile is deleted. This cannot be undone.",
            confirmText = "Delete",
            onConfirm = { isDialogShown = false },
            dismissText = "Cancel",
            onDismiss = { isDialogShown = false },
            style = ESDialogDefaults.Style.DESTRUCTIVE,
        )
    }
}

@Composable
private fun Swatches() {
    val colors = ESTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(ESTheme.spacing.xxs)) {
        listOf(colors.background, colors.surface, colors.surfaceHigh, colors.surfaceHighest, colors.outlineVariant, colors.outline, colors.primary, colors.error, colors.success)
            .forEach { color -> Swatch(color = color) }
    }
}

@Composable
private fun Swatch(color: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color = color, shape = ESTheme.shapes.small),
    )
}
