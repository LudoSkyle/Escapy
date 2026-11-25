package fr.skyle.escapy.di

import fr.skyle.escapy.domain.di.FIREBASE_EMULATOR_PROPERTY
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/** The version of the app and its build, as the host knows them: "2.0 (20000)". */
internal const val APP_VERSION_PROPERTY = "escapy.appVersion"

/**
 * Starts Koin with the modules of the app. [appVersion] is shown in About. [useFirebaseEmulator] is
 * set by a debug build only, on demand: the app then calls the Firebase emulators of the Mac (see
 * docs/Backend/README.md).
 */
fun startEscapyKoin(
    useFirebaseEmulator: Boolean,
    appVersion: String,
    platform: KoinApplication.() -> Unit = {},
) {
    startKoin {
        platform()
        properties(
            mapOf(
                FIREBASE_EMULATOR_PROPERTY to useFirebaseEmulator.toString(),
                APP_VERSION_PROPERTY to appVersion,
            ),
        )
        modules(sharedModule)
    }
}
