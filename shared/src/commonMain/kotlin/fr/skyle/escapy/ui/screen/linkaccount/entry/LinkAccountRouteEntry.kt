package fr.skyle.escapy.ui.screen.linkaccount.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.linkaccount.ui.LinkAccountRoute

fun EntryProviderScope<Route>.linkAccountRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.LinkAccount> { route ->
        LinkAccountRoute(
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
        )
    }
}
