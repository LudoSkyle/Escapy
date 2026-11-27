package fr.skyle.escapy.ui.screen.play.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.main.navigation.navigateTo
import fr.skyle.escapy.ui.screen.play.ui.PlayRoute

fun EntryProviderScope<Route>.playRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Play> { route ->
        PlayRoute(
            gameId = route.gameId,
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
            navigateToChat = {
                backStack.navigateTo(route = Route.Chat(gameId = route.gameId))
            },
            navigateToEditMission = { missionId ->
                backStack.navigateTo(route = Route.EditMission(gameId = route.gameId, missionId = missionId))
            },
        )
    }
}
