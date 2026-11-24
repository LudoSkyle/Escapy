package fr.skyle.escapy.ui.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import fr.skyle.escapy.ui.screen.home.entry.homeRouteEntry
import fr.skyle.escapy.ui.screen.welcome.entry.welcomeRouteEntry

/**
 * The screens of the app. The session decides the root of the back stack: a sign-in or a sign-out
 * replaces the whole back stack, so back never returns to a screen of the other side.
 */
@Composable
fun RootNavDisplay(
    isSignedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    val root = rootRoute(isSignedIn = isSignedIn)
    val backStack = rememberSerializable(serializer = NavBackStackSerializer(Route.serializer())) {
        NavBackStack(root)
    }

    LaunchedEffect(root) {
        if (backStack.firstOrNull() != root) {
            backStack.clear()
            backStack.add(root)
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            welcomeRouteEntry()

            homeRouteEntry()
        },
    )
}

internal fun rootRoute(isSignedIn: Boolean): Route =
    if (isSignedIn) Route.Home else Route.Welcome
