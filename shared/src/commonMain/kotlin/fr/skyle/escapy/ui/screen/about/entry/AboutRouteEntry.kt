package fr.skyle.escapy.ui.screen.about.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.about.ui.AboutRoute

fun EntryProviderScope<Route>.aboutRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.About> {
        AboutRoute(
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}
