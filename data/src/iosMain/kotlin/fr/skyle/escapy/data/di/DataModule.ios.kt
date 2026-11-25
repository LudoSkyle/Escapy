package fr.skyle.escapy.data.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import fr.skyle.escapy.data.firebase.FirebaseEmulator
import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.firebase.defaultServices
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformDataModule: Module = module {
    single<FirebaseProvider> {
        val useEmulator = getPropertyOrNull<String>(FirebaseEmulator.PROPERTY) == "true"
        // Built once, at the first call: the emulators can be set only before any use
        val services by lazy {
            when {
                useEmulator -> FirebaseEmulator.services(
                    existingApps = Firebase.apps(),
                    initialize = { options, name -> Firebase.initialize(null, options, name) },
                    applicationId = "1:000000000000:ios:0000000000000000",
                )
                // Started by the app of iosApp, only when the Firebase file is in its bundle
                Firebase.apps().isNotEmpty() -> defaultServices()
                else -> null
            }
        }
        FirebaseProvider { services }
    }
}
