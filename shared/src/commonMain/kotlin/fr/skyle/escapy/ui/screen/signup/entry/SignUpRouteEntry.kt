package fr.skyle.escapy.ui.screen.signup.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.signup.ui.SignUpRoute

fun EntryProviderScope<Route>.signUpRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.SignUp> { route ->
        SignUpRoute(
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
        )
    }
}
