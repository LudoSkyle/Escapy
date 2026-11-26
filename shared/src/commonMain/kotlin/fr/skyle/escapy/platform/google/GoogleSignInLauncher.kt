package fr.skyle.escapy.platform.google

import androidx.compose.runtime.Composable
import fr.skyle.escapy.domain.model.GoogleTokens
import kotlinx.coroutines.CancellationException

/**
 * The native sign-in of Google, which gives the tokens Firebase signs in with: Credential Manager
 * on Android, GoogleSignIn on iOS. Asked from a Route, on a click: it shows the sheet of the platform.
 */
interface GoogleSignInLauncher {

    /** False without the Google configuration in the Firebase files (a fork, the CI): no Google button. */
    val isAvailable: Boolean

    /** The tokens of the account the user chose, or null when they closed the sign-in. */
    suspend fun requestTokens(): GoogleTokens?
}

@Composable
expect fun rememberGoogleSignInLauncher(): GoogleSignInLauncher

/** The tokens, or null when the user closed the sign-in or when it failed, told to [onFailure]. */
suspend fun GoogleSignInLauncher.requestTokensOrNull(onFailure: (Exception) -> Unit): GoogleTokens? =
    try {
        requestTokens()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        onFailure(exception)
        null
    }
