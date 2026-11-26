package fr.skyle.escapy.ui.main.navigation

import androidx.navigation3.runtime.NavBackStack

/*
 * A second tap during a transition reaches the screen that is going: each move checks the screen it
 * comes from. A back stack emptied that way crashes NavDisplay, and a screen opened twice needs two
 * backs.
 */

/** Opens [route] over the current screen, unless it is already the current one. */
fun NavBackStack<Route>.navigateTo(route: Route) {
    if (lastOrNull() != route) {
        add(route)
    }
}

/** Closes [from], if it is still the current screen, and never the root. */
fun NavBackStack<Route>.navigateBackFrom(from: Route) {
    if (size > 1 && lastOrNull() == from) {
        removeAt(lastIndex)
    }
}

/** Replaces [from], if it is still the current screen, by [to]: back from [to] skips [from]. */
fun NavBackStack<Route>.replace(from: Route, to: Route) {
    if (lastOrNull() == from) {
        set(lastIndex, to)
    }
}
