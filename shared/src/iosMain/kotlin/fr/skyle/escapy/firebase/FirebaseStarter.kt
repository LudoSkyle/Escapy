package fr.skyle.escapy.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import platform.Foundation.NSBundle

/**
 * Starts Firebase, before Koin, when the Firebase file of the private submodule was copied into
 * the app. Without it (a fork, the CI), the app launches signed out and never calls Firebase:
 * configuring Firebase without its file would stop the app.
 */
fun startFirebase() {
    if (NSBundle.mainBundle.pathForResource(name = "GoogleService-Info", ofType = "plist") != null) {
        Firebase.initialize()
    }
}
