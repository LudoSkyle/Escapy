package fr.skyle.escapy.ui.screen.changeemail.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.changeemail.ui.ChangeEmailRoute

fun EntryProviderScope<Route>.changeEmailRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.ChangeEmail> {
        ChangeEmailRoute(
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}
