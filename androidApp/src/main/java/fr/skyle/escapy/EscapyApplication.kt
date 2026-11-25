package fr.skyle.escapy

import android.app.Application
import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import fr.skyle.escapy.di.startEscapyKoin
import org.koin.android.ext.koin.androidContext

class EscapyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Logcat in debug only, until Crashlytics collects the logs of a release build
        Logger.setLogWriters(if (BuildConfig.DEBUG) listOf(platformLogWriter()) else emptyList())

        startEscapyKoin(useFirebaseEmulator = BuildConfig.USE_FIREBASE_EMULATOR) {
            androidContext(this@EscapyApplication)
        }
    }
}
