package fr.skyle.escapy.ui.screen.home.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateTo
import fr.skyle.escapy.ui.screen.home.ui.HomeRoute

fun EntryProviderScope<Route>.homeRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Home> {
        HomeRoute(
            navigateToProfile = {
                backStack.navigateTo(route = Route.Profile)
            },
            navigateToCreateGame = {
                backStack.navigateTo(route = Route.CreateGame)
            },
            navigateToGame = { gameId ->
                backStack.navigateTo(route = Route.WaitingRoom(gameId = gameId))
            },
        )
    }
}
