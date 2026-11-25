package fr.skyle.escapy.ui.screen.forgotpassword.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.forgotpassword.ui.ForgotPasswordRoute

fun EntryProviderScope<Route>.forgotPasswordRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.ForgotPassword> { route ->
        ForgotPasswordRoute(
            email = route.email,
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}
