package fr.skyle.escapy.ui.screen.home.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.home.ui.HomeRoute

fun EntryProviderScope<Route>.homeRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Home> {
        HomeRoute(
            navigateToProfile = {
                backStack.add(Route.Profile)
            },
            navigateToCreateGame = {
                backStack.add(Route.CreateGame)
            },
            navigateToGame = { gameId ->
                backStack.add(Route.WaitingRoom(gameId = gameId))
            },
        )
    }
}
