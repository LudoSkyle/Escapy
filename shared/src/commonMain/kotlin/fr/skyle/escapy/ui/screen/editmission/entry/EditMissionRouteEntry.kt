package fr.skyle.escapy.ui.screen.editmission.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.editmission.ui.EditMissionRoute

fun EntryProviderScope<Route>.editMissionRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.EditMission> { route ->
        EditMissionRoute(
            gameId = route.gameId,
            missionId = route.missionId,
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
        )
    }
}
