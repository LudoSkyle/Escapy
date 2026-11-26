package fr.skyle.escapy.ui.screen.editusername.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.editusername.ui.EditUsernameRoute

fun EntryProviderScope<Route>.editUsernameRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.EditUsername> { route ->
        EditUsernameRoute(
            currentUsername = route.currentUsername,
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
        )
    }
}
