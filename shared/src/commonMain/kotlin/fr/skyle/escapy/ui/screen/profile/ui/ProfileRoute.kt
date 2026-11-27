package fr.skyle.escapy.ui.screen.profile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.skyle.escapy.designsystem.core.snackbar.rememberESSnackbarState
import fr.skyle.escapy.ui.core.error.accountErrorMessages
import fr.skyle.escapy.ui.core.event.ObserveEvents
import fr.skyle.escapy.ui.screen.profile.ui.ProfileViewModel.ProfileEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoute(
    navigateBack: () -> Unit,
    navigateToEditUsername: (currentUsername: String) -> Unit,
    navigateToEditAvatar: () -> Unit,
    navigateToLinkAccount: () -> Unit,
    navigateToChangeEmail: () -> Unit,
    navigateToChangePassword: () -> Unit,
    navigateToDeleteAccount: () -> Unit,
    navigateToAbout: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = rememberESSnackbarState()
    val errorMessages = accountErrorMessages()

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is ProfileEvent.ShowError -> snackbarState.show(message = errorMessages.getValue(event.error))
        }
    }

    ProfileScreen(
        uiState = uiState,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onEditUsernameClicked = { uiState.profile?.let { profile -> navigateToEditUsername(profile.username) } },
        onEditAvatarClicked = navigateToEditAvatar,
        onLinkAccountClicked = navigateToLinkAccount,
        onChangeEmailClicked = navigateToChangeEmail,
        onChangePasswordClicked = navigateToChangePassword,
        onAboutClicked = navigateToAbout,
        onSignOutClicked = viewModel::onSignOutClicked,
        onDeleteAccountClicked = navigateToDeleteAccount,
        onGuestSignOutConfirmed = viewModel::onGuestSignOutConfirmed,
        onGuestSignOutDismissed = viewModel::onGuestSignOutDismissed,
    )
}
