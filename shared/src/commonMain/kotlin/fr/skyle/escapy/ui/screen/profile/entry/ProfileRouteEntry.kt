package fr.skyle.escapy.ui.screen.profile.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateTo
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.profile.ui.ProfileRoute

fun EntryProviderScope<Route>.profileRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Profile> { route ->
        ProfileRoute(
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
            navigateToEditUsername = { currentUsername ->
                backStack.navigateTo(route = Route.EditUsername(currentUsername = currentUsername))
            },
            navigateToEditAvatar = {
                backStack.navigateTo(route = Route.EditAvatar)
            },
            navigateToLinkAccount = {
                backStack.navigateTo(route = Route.LinkAccount)
            },
            navigateToChangeEmail = {
                backStack.navigateTo(route = Route.ChangeEmail)
            },
            navigateToChangePassword = {
                backStack.navigateTo(route = Route.ChangePassword)
            },
            navigateToDeleteAccount = {
                backStack.navigateTo(route = Route.DeleteAccount)
            },
            navigateToAbout = {
                backStack.navigateTo(route = Route.About)
            },
        )
    }
}
