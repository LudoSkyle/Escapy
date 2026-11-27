package fr.skyle.escapy.ui.screen.chat.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.chat.ui.ChatRoute

fun EntryProviderScope<Route>.chatRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Chat> { route ->
        ChatRoute(
            gameId = route.gameId,
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
        )
    }
}
