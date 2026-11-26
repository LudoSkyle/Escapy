package fr.skyle.escapy.domain.model

/**
 * What Google gives the app once the user chose an account. Firebase signs in with the ID token;
 * its iOS SDK also requires the access token, which Android does not give.
 */
data class GoogleTokens(
    val idToken: String,
    val accessToken: String?,
)
