package fr.skyle.escapy.data.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.apps
import fr.skyle.escapy.data.firebase.FirebaseStatus
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformDataModule: Module = module {
    // Started by the google-services plugin, which the build applies only with the Firebase file
    single<FirebaseStatus> {
        val context = androidContext()
        FirebaseStatus { Firebase.apps(context).isNotEmpty() }
    }
}
