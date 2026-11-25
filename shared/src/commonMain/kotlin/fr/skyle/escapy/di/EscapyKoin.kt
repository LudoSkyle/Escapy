package fr.skyle.escapy.di

import fr.skyle.escapy.domain.di.FIREBASE_EMULATOR_PROPERTY
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

/**
 * Starts Koin with the modules of the app. [useFirebaseEmulator] is set by a debug build only, on
 * demand: the app then calls the Firebase emulators of the Mac (see docs/Backend/README.md).
 */
fun startEscapyKoin(
    useFirebaseEmulator: Boolean,
    platform: KoinApplication.() -> Unit = {},
) {
    startKoin {
        platform()
        properties(mapOf(FIREBASE_EMULATOR_PROPERTY to useFirebaseEmulator.toString()))
        modules(sharedModule)
    }
}
