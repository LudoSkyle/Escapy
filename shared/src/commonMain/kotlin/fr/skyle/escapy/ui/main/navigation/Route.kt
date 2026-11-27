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
    data object CreateGame : Route

    /** The camera, to join a game by its QR code. */
    @Serializable
    data object ScanCode : Route

    /** A game before it starts, opened by its id: the screen reads the game from Firestore. */
    @Serializable
    data class WaitingRoom(val gameId: String) : Route

    /** A started game, then its end: the timer, and the recap once it is over. */
    @Serializable
    data class Play(val gameId: String) : Route

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
