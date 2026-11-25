package fr.skyle.escapy.di

import platform.Foundation.NSProcessInfo
import kotlin.experimental.ExperimentalNativeApi

/**
 * Starts Koin. Called by the SwiftUI app of iosApp when it starts, as EscapyApplication does on
 * Android. A debug build launched with ESCAPY_FIREBASE_EMULATOR=1 calls the Firebase emulators.
 */
@OptIn(ExperimentalNativeApi::class)
fun initKoin() {
    startEscapyKoin(
        useFirebaseEmulator = Platform.isDebugBinary &&
            NSProcessInfo.processInfo.environment["ESCAPY_FIREBASE_EMULATOR"] == "1",
    )
}
