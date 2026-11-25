package fr.skyle.escapy.ui.screen.editavatar.entry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import fr.skyle.escapy.ui.main.navigation.Route
import fr.skyle.escapy.ui.screen.editavatar.ui.EditAvatarRoute

fun EntryProviderScope<Route>.editAvatarRouteEntry(
    backStack: NavBackStack<Route>,
) {
    entry<Route.EditAvatar> {
        EditAvatarRoute(
            navigateBack = {
                backStack.removeLastOrNull()
            },
        )
    }
}
