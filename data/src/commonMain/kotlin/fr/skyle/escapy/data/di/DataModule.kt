package fr.skyle.escapy.data.di

import fr.skyle.escapy.data.repository.account.AccountRepositoryImpl
import fr.skyle.escapy.data.repository.account.api.AccountRepository
import fr.skyle.escapy.data.repository.auth.AuthRepositoryImpl
import fr.skyle.escapy.data.repository.auth.api.AuthRepository
import fr.skyle.escapy.data.repository.clock.ServerClockRepositoryImpl
import fr.skyle.escapy.data.repository.clock.api.ServerClockRepository
import fr.skyle.escapy.data.repository.game.GameRepositoryImpl
import fr.skyle.escapy.data.repository.game.api.GameRepository
import fr.skyle.escapy.data.repository.profile.ProfileRepositoryImpl
import fr.skyle.escapy.data.repository.profile.api.ProfileRepository
import fr.skyle.escapy.data.repository.session.SessionRepositoryImpl
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    includes(platformDataModule)

    singleOf(::SessionRepositoryImpl) bind SessionRepository::class
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::ProfileRepositoryImpl) bind ProfileRepository::class
    singleOf(::AccountRepositoryImpl) bind AccountRepository::class
    singleOf(::GameRepositoryImpl) bind GameRepository::class
    singleOf(::ServerClockRepositoryImpl) bind ServerClockRepository::class
}

// What each platform answers differently, such as which Firebase app to call
internal expect val platformDataModule: Module

/** The Koin property a debug host sets to "true" to use the Firebase emulators. */
const val FIREBASE_EMULATOR_PROPERTY = "escapy.firebaseEmulator"
