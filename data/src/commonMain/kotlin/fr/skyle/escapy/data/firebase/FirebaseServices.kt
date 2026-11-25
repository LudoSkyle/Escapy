package fr.skyle.escapy.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.functions.FirebaseFunctions
import dev.gitlive.firebase.functions.functions

/** The region of the Cloud Functions, next to the Firestore database. */
internal const val FUNCTIONS_REGION = "europe-west1"

/** The Firebase services the Repositories call, of the real project or of the emulators. */
internal class FirebaseServices(
    val auth: FirebaseAuth,
    val firestore: FirebaseFirestore,
    val functions: FirebaseFunctions,
)

/**
 * Gives the Firebase services, or null when Firebase did not start: a build without the private
 * Firebase files (a fork, the CI) launches signed out and never calls Firebase.
 */
internal fun interface FirebaseProvider {
    fun services(): FirebaseServices?
}

/** The services of the default app, the one started from the Firebase file of the private submodule. */
internal fun defaultServices(): FirebaseServices =
    FirebaseServices(
        auth = Firebase.auth,
        firestore = Firebase.firestore,
        functions = Firebase.functions(region = FUNCTIONS_REGION),
    )

/**
 * The emulators, opted in by a debug build only (see docs/Backend/README.md). They run in an app of
 * their own, on the demo project: with or without the Firebase file, their session and their cache
 * never mix with those of the real project.
 */
internal object FirebaseEmulator {

    /** The Koin property a debug host sets to "true" to use the emulators. */
    const val PROPERTY = fr.skyle.escapy.data.di.FIREBASE_EMULATOR_PROPERTY

    private const val APP_NAME = "emulator"
    private const val PROJECT_ID = "demo-escapy"
    // The emulators accept any key, but the Functions SDK of Android checks it looks like one (39
    // characters from AIza): a fake key, of no project. An Android device reaches the Mac through adb reverse
    private const val API_KEY = "AIzaSyDemoKeyOfTheEscapyFirebaseEmulato"
    private const val HOST = "localhost"

    fun services(existingApps: List<FirebaseApp>, initialize: (FirebaseOptions, String) -> FirebaseApp, applicationId: String): FirebaseServices {
        val app = existingApps.firstOrNull { app -> app.name == APP_NAME }
            ?: initialize(
                FirebaseOptions(applicationId = applicationId, apiKey = API_KEY, projectId = PROJECT_ID),
                APP_NAME,
            )
        val services = FirebaseServices(
            auth = Firebase.auth(app),
            firestore = Firebase.firestore(app),
            functions = Firebase.functions(app, FUNCTIONS_REGION),
        )
        services.auth.useEmulator(host = HOST, port = 9099)
        services.firestore.useEmulator(host = HOST, port = 8080)
        services.functions.useEmulator(host = HOST, port = 5001)
        return services
    }
}
