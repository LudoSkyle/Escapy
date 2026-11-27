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
import fr.skyle.escapy.ui.screen.about.entry.aboutRouteEntry
import fr.skyle.escapy.ui.screen.changeemail.entry.changeEmailRouteEntry
import fr.skyle.escapy.ui.screen.changepassword.entry.changePasswordRouteEntry
import fr.skyle.escapy.ui.screen.chat.entry.chatRouteEntry
import fr.skyle.escapy.ui.screen.creategame.entry.createGameRouteEntry
import fr.skyle.escapy.ui.screen.deleteaccount.entry.deleteAccountRouteEntry
import fr.skyle.escapy.ui.screen.editavatar.entry.editAvatarRouteEntry
import fr.skyle.escapy.ui.screen.editusername.entry.editUsernameRouteEntry
import fr.skyle.escapy.ui.screen.linkaccount.entry.linkAccountRouteEntry
import fr.skyle.escapy.ui.screen.play.entry.playRouteEntry
import fr.skyle.escapy.ui.screen.profile.entry.profileRouteEntry
import fr.skyle.escapy.ui.screen.forgotpassword.entry.forgotPasswordRouteEntry
import fr.skyle.escapy.ui.screen.home.entry.homeRouteEntry
import fr.skyle.escapy.ui.screen.scancode.entry.scanCodeRouteEntry
import fr.skyle.escapy.ui.screen.signin.entry.signInRouteEntry
import fr.skyle.escapy.ui.screen.signup.entry.signUpRouteEntry
import fr.skyle.escapy.ui.screen.waitingroom.entry.waitingRoomRouteEntry
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
            // Never the root: the system back on it leaves the app instead
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        transitionSpec = { forwardTransition() },
        popTransitionSpec = { backTransition() },
        // The system back gesture follows the finger with the same slide
        predictivePopTransitionSpec = { backTransition() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            welcomeRouteEntry(backStack = backStack)

            signInRouteEntry(backStack = backStack)

            signUpRouteEntry(backStack = backStack)

            forgotPasswordRouteEntry(backStack = backStack)

            homeRouteEntry(backStack = backStack)

            createGameRouteEntry(backStack = backStack)

            scanCodeRouteEntry(backStack = backStack)

            waitingRoomRouteEntry(backStack = backStack)
            playRouteEntry(backStack = backStack)
            chatRouteEntry(backStack = backStack)

            profileRouteEntry(backStack = backStack)

            editUsernameRouteEntry(backStack = backStack)

            editAvatarRouteEntry(backStack = backStack)

            linkAccountRouteEntry(backStack = backStack)

            changeEmailRouteEntry(backStack = backStack)

            changePasswordRouteEntry(backStack = backStack)

            deleteAccountRouteEntry(backStack = backStack)

            aboutRouteEntry(backStack = backStack)
        },
    )
}

internal fun rootRoute(isSignedIn: Boolean): Route =
    if (isSignedIn) Route.Home else Route.Welcome
