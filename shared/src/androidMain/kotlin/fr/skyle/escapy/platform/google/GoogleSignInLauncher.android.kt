package fr.skyle.escapy.platform.google

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import fr.skyle.escapy.domain.model.GoogleTokens

@Composable
actual fun rememberGoogleSignInLauncher(): GoogleSignInLauncher {
    // The context of the activity: Credential Manager shows its sheet over it
    val context = LocalContext.current
    return remember(context) { CredentialManagerGoogleSignIn(context = context) }
}

private class CredentialManagerGoogleSignIn(
    private val context: Context,
) : GoogleSignInLauncher {

    // The web client of the Firebase project, written by the Google Services plugin from the Firebase
    // file once Google sign-in is enabled: looked up by name, absent from a build without the file.
    // androidApp keeps it from the resource shrinker (res/raw/keep.xml)
    @SuppressLint("DiscouragedApi")
    private val webClientId: String? =
        context.resources.getIdentifier(WEB_CLIENT_ID_RESOURCE, "string", context.packageName)
            .takeIf { id -> id != 0 }
            ?.let(context::getString)

    override val isAvailable: Boolean
        get() = webClientId != null

    override suspend fun requestTokens(): GoogleTokens? {
        val serverClientId = webClientId ?: return null
        // The button flow of Google: the user picks an account, or adds one
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build())
            .build()
        val credential = try {
            CredentialManager.create(context).getCredential(context = context, request = request).credential
        } catch (exception: GetCredentialCancellationException) {
            return null
        }
        check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "Unexpected credential from Credential Manager: ${credential.type}"
        }
        // Credential Manager gives no access token, which Firebase does not need on Android
        return GoogleTokens(idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken, accessToken = null)
    }

    private companion object {
        const val WEB_CLIENT_ID_RESOURCE = "default_web_client_id"
    }
}
