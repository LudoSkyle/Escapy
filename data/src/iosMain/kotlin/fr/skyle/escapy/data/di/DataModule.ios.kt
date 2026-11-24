package fr.skyle.escapy.data.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.apps
import fr.skyle.escapy.data.firebase.FirebaseStatus
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformDataModule: Module = module {
    // Started by the app of iosApp, only when the Firebase file is in its bundle
    single<FirebaseStatus> { FirebaseStatus { Firebase.apps().isNotEmpty() } }
}
