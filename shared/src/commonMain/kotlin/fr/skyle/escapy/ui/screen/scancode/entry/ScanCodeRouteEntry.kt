package fr.skyle.escapy.ui.screen.scancode.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.main.navigation.replace
import fr.skyle.escapy.ui.screen.scancode.ui.ScanCodeRoute

fun EntryProviderScope<Route>.scanCodeRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.ScanCode> { route ->
        ScanCodeRoute(
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
            navigateToGame = { gameId ->
                // Back from the waiting room returns home, not to the camera
                backStack.replace(from = route, to = Route.WaitingRoom(gameId = gameId))
            },
        )
    }
}
