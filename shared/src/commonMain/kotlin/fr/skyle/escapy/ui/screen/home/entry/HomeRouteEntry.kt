package fr.skyle.escapy.ui.screen.home.entry

import androidx.navigation3.runtime.EntryProviderScope
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.home.ui.HomeRoute

fun EntryProviderScope<Route>.homeRouteEntry() {
    entry<Route.Home> {
        HomeRoute()
    }
}
