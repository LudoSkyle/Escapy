package fr.skyle.escapy.ui.screen.welcome.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.main.navigation.navigateTo
import fr.skyle.escapy.ui.screen.welcome.ui.WelcomeRoute

fun EntryProviderScope<Route>.welcomeRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.Welcome> {
        WelcomeRoute(
            navigateToSignIn = {
                backStack.navigateTo(route = Route.SignIn)
            },
            navigateToSignUp = {
                backStack.navigateTo(route = Route.SignUp)
            },
        )
    }
}
