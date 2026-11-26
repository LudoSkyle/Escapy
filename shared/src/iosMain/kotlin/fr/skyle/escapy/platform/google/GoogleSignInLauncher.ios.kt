package fr.skyle.escapy.platform.google

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import fr.skyle.escapy.domain.model.GoogleTokens
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.NSURL
import platform.Foundation.dictionaryWithContentsOfFile
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import swiftPMImport.fr.skyle.escapy.shared.GIDConfiguration
import swiftPMImport.fr.skyle.escapy.shared.GIDSignIn
import swiftPMImport.fr.skyle.escapy.shared.kGIDSignInErrorCodeCanceled
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
actual fun rememberGoogleSignInLauncher(): GoogleSignInLauncher =
    remember { GoogleSignInIos() }

/** Gives GoogleSignIn the URL the app was opened with: true when it was its own. Called by the app. */
@OptIn(ExperimentalForeignApi::class)
fun handleGoogleSignInUrl(url: NSURL): Boolean =
    GIDSignIn.sharedInstance.handleURL(url)

private class GoogleSignInIos : GoogleSignInLauncher {

    // The iOS client of the Firebase project, in the Firebase file once Google sign-in is enabled
    private val clientId: String? =
        NSBundle.mainBundle.pathForResource(name = "GoogleService-Info", ofType = "plist")
            ?.let { path -> NSDictionary.dictionaryWithContentsOfFile(path) }
            ?.get("CLIENT_ID") as? String

    override val isAvailable: Boolean
        get() = clientId != null

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun requestTokens(): GoogleTokens? {
        val clientId = clientId ?: return null
        // Looked for at the time of the click: the screen shown now, over which the sheet opens
        val presenter = topViewController() ?: error("No view controller to present the sign-in of Google from")
        val signIn = GIDSignIn.sharedInstance
        signIn.configuration = GIDConfiguration(clientID = clientId)
        return suspendCancellableCoroutine { continuation ->
            signIn.signInWithPresentingViewController(presenter) { result, error ->
                when {
                    error?.code == kGIDSignInErrorCodeCanceled -> continuation.resume(null)
                    error != null -> continuation.resumeWithException(IllegalStateException(error.localizedDescription))
                    else -> {
                        val user = result?.user
                        val idToken = user?.idToken?.tokenString
                        // Firebase on iOS requires the access token along with the ID token
                        continuation.resume(idToken?.let { token -> GoogleTokens(idToken = token, accessToken = user.accessToken.tokenString) })
                    }
                }
            }
        }
    }

    private fun topViewController(): UIViewController? {
        val window = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
            .firstOrNull { window -> window.isKeyWindow() }
        var controller = window?.rootViewController
        while (controller?.presentedViewController != null) {
            controller = controller.presentedViewController
        }
        return controller
    }
}
