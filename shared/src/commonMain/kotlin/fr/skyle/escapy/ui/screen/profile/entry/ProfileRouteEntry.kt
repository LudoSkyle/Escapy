package fr.skyle.escapy.ui.screen.profile.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.profile.ui.ProfileRoute

fun EntryProviderScope<Route>.profileRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Profile> {
        ProfileRoute(
            navigateBack = {
                backStack.removeLastOrNull()
            },
            navigateToEditUsername = { currentUsername ->
                backStack.add(Route.EditUsername(currentUsername = currentUsername))
            },
            navigateToEditAvatar = {
                backStack.add(Route.EditAvatar)
            },
            navigateToLinkAccount = {
                backStack.add(Route.LinkAccount)
            },
            navigateToChangeEmail = {
                backStack.add(Route.ChangeEmail)
            },
            navigateToChangePassword = {
                backStack.add(Route.ChangePassword)
            },
            navigateToDeleteAccount = {
                backStack.add(Route.DeleteAccount)
            },
            navigateToAbout = {
                backStack.add(Route.About)
            },
        )
    }
}
