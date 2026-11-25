package fr.skyle.escapy.ui.screen.signin.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.signin.ui.SignInRoute

fun EntryProviderScope<Route>.signInRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.SignIn> {
        SignInRoute(
            navigateBack = {
                backStack.removeLastOrNull()
            },
            navigateToForgotPassword = { email ->
                backStack.add(Route.ForgotPassword(email = email))
            },
        )
    }
}
