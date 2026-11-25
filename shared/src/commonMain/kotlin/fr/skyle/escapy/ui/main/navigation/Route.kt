package fr.skyle.escapy.ui.main.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {

    /** The first screen without a session: sign in, sign up or play as a guest. */
    @Serializable
    data object Welcome : Route

    @Serializable
    data object SignIn : Route

    @Serializable
    data object SignUp : Route

    /** Opened from the sign-in screen, with the email already written there. */
    @Serializable
    data class ForgotPassword(val email: String) : Route

    /** The first screen with a session: join or create a game. */
    @Serializable
    data object Home : Route

    @Serializable
    data object Profile : Route

    /** Opened from the profile, with the username shown there. */
    @Serializable
    data class EditUsername(val currentUsername: String) : Route

    @Serializable
    data object EditAvatar : Route

    @Serializable
    data object LinkAccount : Route

    @Serializable
    data object ChangeEmail : Route

    @Serializable
    data object ChangePassword : Route

    @Serializable
    data object DeleteAccount : Route

    @Serializable
    data object About : Route
}
