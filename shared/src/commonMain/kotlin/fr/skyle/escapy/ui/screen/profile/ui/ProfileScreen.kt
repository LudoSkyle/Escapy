package fr.skyle.escapy.ui.screen.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.button.ESButton
import fr.skyle.escapy.designsystem.core.button.ESButtonDefaults
import fr.skyle.escapy.designsystem.core.card.ESCard
import fr.skyle.escapy.designsystem.core.card.ESCardDefaults
import fr.skyle.escapy.designsystem.core.dialog.ESDialog
import fr.skyle.escapy.designsystem.core.dialog.ESDialogDefaults
import fr.skyle.escapy.designsystem.core.divider.ESDivider
import fr.skyle.escapy.designsystem.core.icon.ESIcon
import fr.skyle.escapy.designsystem.core.icon.ESIconDefaults
import fr.skyle.escapy.designsystem.core.row.ESRow
import fr.skyle.escapy.designsystem.core.row.ESRowDefaults
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructure
import fr.skyle.escapy.designsystem.core.screenstructure.ESScreenStructureDefaults
import fr.skyle.escapy.designsystem.core.snackbar.ESSnackbarState
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.core.topappbar.ESTopAppBar
import fr.skyle.escapy.designsystem.theme.ESTheme
import fr.skyle.escapy.domain.model.Profile
import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.action_cancel
import fr.skyle.escapy.resources.ic_edit
import fr.skyle.escapy.resources.profile_about
import fr.skyle.escapy.resources.profile_account
import fr.skyle.escapy.resources.profile_change_avatar
import fr.skyle.escapy.resources.profile_change_email
import fr.skyle.escapy.resources.profile_change_password
import fr.skyle.escapy.resources.profile_delete_account
import fr.skyle.escapy.resources.profile_edit_username
import fr.skyle.escapy.resources.profile_general
import fr.skyle.escapy.resources.profile_google_account
import fr.skyle.escapy.resources.profile_guest_warning
import fr.skyle.escapy.resources.profile_link_account
import fr.skyle.escapy.resources.profile_privacy_policy
import fr.skyle.escapy.resources.profile_title
import fr.skyle.escapy.resources.profile_your_avatar
import fr.skyle.escapy.resources.sign_out
import fr.skyle.escapy.resources.sign_out_guest_dialog_message
import fr.skyle.escapy.resources.sign_out_guest_dialog_title
import fr.skyle.escapy.ui.core.avatar.AvatarImage
import fr.skyle.escapy.ui.screen.profile.ui.ProfileViewModel.ProfileUIState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileScreen(
    uiState: ProfileUIState,
    snackbarState: ESSnackbarState,
    onBackClicked: () -> Unit,
    onEditUsernameClicked: () -> Unit,
    onEditAvatarClicked: () -> Unit,
    onLinkAccountClicked: () -> Unit,
    onChangeEmailClicked: () -> Unit,
    onChangePasswordClicked: () -> Unit,
    onAboutClicked: () -> Unit,
    onPrivacyPolicyClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
    onGuestSignOutConfirmed: () -> Unit,
    onGuestSignOutDismissed: () -> Unit,
) {
    ESScreenStructure(
        style = ESScreenStructureDefaults.Style.STANDARD,
        topBar = {
            ESTopAppBar(
                title = stringResource(Res.string.profile_title),
                onBackClick = onBackClicked,
            )
        },
        snackbarState = snackbarState,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(ESTheme.spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val profile = uiState.profile
            if (profile != null) {
                AvatarImage(
                    modifier = Modifier.clickable(
                        onClickLabel = stringResource(Res.string.profile_change_avatar),
                        role = Role.Button,
                        onClick = onEditAvatarClicked,
                    ),
                    avatarId = profile.avatarId,
                    size = 112.dp,
                    contentDescription = stringResource(Res.string.profile_your_avatar),
                )

                Spacer(modifier = Modifier.height(ESTheme.spacing.s))

                Row(
                    modifier = Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(Res.string.profile_edit_username),
                        onClick = onEditUsernameClicked,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ESText(
                        text = profile.username,
                        style = ESTheme.typography.title,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.width(ESTheme.spacing.xs))

                    // The row reads as the name, with the action to change it
                    ESIcon(
                        painter = painterResource(Res.drawable.ic_edit),
                        contentDescription = null,
                        size = ESIconDefaults.Size.SMALL,
                        tint = ESTheme.colors.textSecondary,
                    )
                }

                val email = uiState.email
                if (email != null) {
                    ESText(
                        text = email,
                        style = ESTheme.typography.bodySmall,
                        color = ESTheme.colors.textSecondary,
                    )
                }

                if (uiState.isSignedInWithGoogleOnly) {
                    ESText(
                        text = stringResource(Res.string.profile_google_account),
                        style = ESTheme.typography.bodySmall,
                        color = ESTheme.colors.textSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            if (uiState.isGuest) {
                ESCard(
                    modifier = Modifier.fillMaxWidth(),
                    style = ESCardDefaults.Style.STANDARD,
                ) {
                    Column(modifier = Modifier.padding(ESTheme.spacing.m)) {
                        ESText(
                            text = stringResource(Res.string.profile_guest_warning),
                            style = ESTheme.typography.body,
                        )

                        Spacer(modifier = Modifier.height(ESTheme.spacing.s))

                        ESButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(Res.string.profile_link_account),
                            onClick = onLinkAccountClicked,
                            style = ESButtonDefaults.Style.PRIMARY,
                            size = ESButtonDefaults.Size.MEDIUM,
                        )
                    }
                }
            } else if (uiState.hasPassword) {
                // Google keeps the email and the password of an account that signs in with it only
                SectionTitle(text = stringResource(Res.string.profile_account))

                ESCard(
                    modifier = Modifier.fillMaxWidth(),
                    style = ESCardDefaults.Style.STANDARD,
                ) {
                    ESRow(
                        label = stringResource(Res.string.profile_change_email),
                        onClick = onChangeEmailClicked,
                        style = ESRowDefaults.Style.STANDARD,
                    )

                    ESDivider()

                    ESRow(
                        label = stringResource(Res.string.profile_change_password),
                        onClick = onChangePasswordClicked,
                        style = ESRowDefaults.Style.STANDARD,
                    )
                }
            }

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            SectionTitle(text = stringResource(Res.string.profile_general))

            ESCard(
                modifier = Modifier.fillMaxWidth(),
                style = ESCardDefaults.Style.STANDARD,
            ) {
                ESRow(
                    label = stringResource(Res.string.profile_about),
                    onClick = onAboutClicked,
                    style = ESRowDefaults.Style.STANDARD,
                )

                ESDivider()

                ESRow(
                    label = stringResource(Res.string.profile_privacy_policy),
                    onClick = onPrivacyPolicyClicked,
                    style = ESRowDefaults.Style.STANDARD,
                )

                ESDivider()

                ESRow(
                    label = stringResource(Res.string.profile_delete_account),
                    onClick = onDeleteAccountClicked,
                    style = ESRowDefaults.Style.DESTRUCTIVE,
                )
            }

            Spacer(modifier = Modifier.height(ESTheme.spacing.l))

            ESButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.sign_out),
                onClick = onSignOutClicked,
                style = ESButtonDefaults.Style.SECONDARY,
                size = ESButtonDefaults.Size.LARGE,
                isLoading = uiState.isSigningOut && !uiState.isGuestSignOutDialogShown,
            )
        }
    }

    if (uiState.isGuestSignOutDialogShown) {
        ESDialog(
            title = stringResource(Res.string.sign_out_guest_dialog_title),
            message = stringResource(Res.string.sign_out_guest_dialog_message),
            confirmText = stringResource(Res.string.sign_out),
            onConfirm = onGuestSignOutConfirmed,
            dismissText = stringResource(Res.string.action_cancel),
            onDismiss = onGuestSignOutDismissed,
            style = ESDialogDefaults.Style.DESTRUCTIVE,
            isConfirmLoading = uiState.isSigningOut,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    ESText(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ESTheme.spacing.xs, bottom = ESTheme.spacing.xs),
        text = text,
        style = ESTheme.typography.label,
        color = ESTheme.colors.textSecondary,
    )
}

@Preview
@Composable
private fun PreviewProfileScreen() {
    ESTheme {
        ProfileScreen(
            uiState = ProfileUIState(
                profile = Profile(username = "Player_ABC234", avatarId = 7),
                email = "player@escapy.app",
                hasPassword = true,
            ),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onEditUsernameClicked = {},
            onEditAvatarClicked = {},
            onLinkAccountClicked = {},
            onChangeEmailClicked = {},
            onChangePasswordClicked = {},
            onAboutClicked = {},
            onPrivacyPolicyClicked = {},
            onSignOutClicked = {},
            onDeleteAccountClicked = {},
            onGuestSignOutConfirmed = {},
            onGuestSignOutDismissed = {},
        )
    }
}

@Preview
@Composable
private fun PreviewProfileScreen_Guest() {
    ESTheme {
        ProfileScreen(
            uiState = ProfileUIState(
                profile = Profile(username = "Player_ABC234", avatarId = 7),
                isGuest = true,
            ),
            snackbarState = rememberESSnackbarState(),
            onBackClicked = {},
            onEditUsernameClicked = {},
            onEditAvatarClicked = {},
            onLinkAccountClicked = {},
            onChangeEmailClicked = {},
            onChangePasswordClicked = {},
            onAboutClicked = {},
            onPrivacyPolicyClicked = {},
            onSignOutClicked = {},
            onDeleteAccountClicked = {},
            onGuestSignOutConfirmed = {},
            onGuestSignOutDismissed = {},
        )
    }
}
