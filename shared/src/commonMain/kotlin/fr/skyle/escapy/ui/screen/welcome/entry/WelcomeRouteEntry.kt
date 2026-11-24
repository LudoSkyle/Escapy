package fr.skyle.escapy.ui.screen.welcome.entry

import androidx.navigation3.runtime.EntryProviderScope
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.welcome.ui.WelcomeRoute

fun EntryProviderScope<Route>.welcomeRouteEntry() {
    entry<Route.Welcome> {
        WelcomeRoute()
    }
}
