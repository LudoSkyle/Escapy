package fr.skyle.escapy.ui.main.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    /** The first screen without a session: sign in, sign up or play as a guest. */
    @Serializable
    data object Welcome : Route

    /** The first screen with a session: join or create a game. */
    @Serializable
    data object Home : Route
}
