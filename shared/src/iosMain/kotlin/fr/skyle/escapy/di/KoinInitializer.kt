package fr.skyle.escapy.di

import org.koin.core.context.startKoin

/** Starts Koin. Called by the SwiftUI app of iosApp when it starts, as EscapyApplication does on Android. */
fun initKoin() {
    startKoin {
        modules(sharedModule)
    }
}
