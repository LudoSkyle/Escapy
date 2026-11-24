package fr.skyle.escapy.data.firebase

/**
 * Whether Firebase started. A build without the private Firebase files (a fork, the CI) has no
 * Firebase: the app still launches, signed out, and nothing calls Firebase.
 */
internal fun interface FirebaseStatus {
    fun isConfigured(): Boolean
}
