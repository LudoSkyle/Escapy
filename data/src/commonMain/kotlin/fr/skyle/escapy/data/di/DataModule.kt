package fr.skyle.escapy.data.di

import fr.skyle.escapy.data.repository.session.SessionRepositoryImpl
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule = module {
    includes(platformDataModule)

    single<SessionRepository> { SessionRepositoryImpl(firebaseStatus = get()) }
}

// What each platform answers differently, such as whether Firebase started
internal expect val platformDataModule: Module
