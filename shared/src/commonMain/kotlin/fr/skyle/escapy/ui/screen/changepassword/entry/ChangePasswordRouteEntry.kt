package fr.skyle.escapy.ui.screen.changepassword.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.changepassword.ui.ChangePasswordRoute

fun EntryProviderScope<Route>.changePasswordRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.ChangePassword> { route ->
        ChangePasswordRoute(
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
        )
    }
}
