package fr.skyle.escapy.ui.screen.signin.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateTo
import fr.skyle.escapy.ui.main.navigation.navigateBackFrom
import fr.skyle.escapy.ui.screen.signin.ui.SignInRoute

fun EntryProviderScope<Route>.signInRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.SignIn> { route ->
        SignInRoute(
            navigateBack = {
                backStack.navigateBackFrom(from = route)
            },
            navigateToForgotPassword = { email ->
                backStack.navigateTo(route = Route.ForgotPassword(email = email))
            },
        )
    }
}
