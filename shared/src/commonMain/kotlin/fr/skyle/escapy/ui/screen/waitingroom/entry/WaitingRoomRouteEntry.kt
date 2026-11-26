package fr.skyle.escapy.ui.screen.waitingroom.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.waitingroom.ui.WaitingRoomRoute

fun EntryProviderScope<Route>.waitingRoomRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.WaitingRoom> { route ->
        WaitingRoomRoute(
            gameId = route.gameId,
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}
