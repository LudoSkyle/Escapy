package fr.skyle.escapy.ui.screen.creategame.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.replace
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.creategame.ui.CreateGameRoute

fun EntryProviderScope<Route>.createGameRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.CreateGame> { route ->
        CreateGameRoute(
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
            navigateToGame = { gameId ->
                // Back from the waiting room returns home, not to a form already sent
                backStack.replace(from = route, to = Route.WaitingRoom(gameId = gameId))
            },
        )
    }
}
