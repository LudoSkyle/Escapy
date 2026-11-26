package fr.skyle.escapy.ui.screen.deleteaccount.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.deleteaccount.ui.DeleteAccountRoute

fun EntryProviderScope<Route>.deleteAccountRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.DeleteAccount> { route ->
        DeleteAccountRoute(
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
        )
    }
}
