package fr.skyle.escapy.domain.di

import fr.skyle.escapy.data.di.FIREBASE_EMULATOR_PROPERTY as DATA_FIREBASE_EMULATOR_PROPERTY
import fr.skyle.escapy.data.di.dataModule
import fr.skyle.escapy.domain.usecase.auth.SendPasswordResetUseCase
import fr.skyle.escapy.domain.usecase.auth.SendPasswordResetUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SignInAsGuestUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInAsGuestUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SignInWithEmailUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInWithEmailUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SignOutUseCase
import fr.skyle.escapy.domain.usecase.auth.SignOutUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SignUpWithEmailUseCase
import fr.skyle.escapy.domain.usecase.auth.SignUpWithEmailUseCaseImpl
import fr.skyle.escapy.domain.usecase.profile.EnsureProfileUseCase
import fr.skyle.escapy.domain.usecase.profile.EnsureProfileUseCaseImpl
import fr.skyle.escapy.domain.usecase.profile.WatchProfileUseCase
import fr.skyle.escapy.domain.usecase.profile.WatchProfileUseCaseImpl
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)

    // Session
    factoryOf(::WatchSessionUseCaseImpl) bind WatchSessionUseCase::class

    // Authentication
    factoryOf(::SignInWithEmailUseCaseImpl) bind SignInWithEmailUseCase::class
    factoryOf(::SignUpWithEmailUseCaseImpl) bind SignUpWithEmailUseCase::class
    factoryOf(::SignInAsGuestUseCaseImpl) bind SignInAsGuestUseCase::class
    factoryOf(::SendPasswordResetUseCaseImpl) bind SendPasswordResetUseCase::class
    factoryOf(::SignOutUseCaseImpl) bind SignOutUseCase::class

    // Profile
    factoryOf(::WatchProfileUseCaseImpl) bind WatchProfileUseCase::class
    factoryOf(::EnsureProfileUseCaseImpl) bind EnsureProfileUseCase::class
}

/** The Koin property a debug host sets to "true" to use the Firebase emulators. */
const val FIREBASE_EMULATOR_PROPERTY = DATA_FIREBASE_EMULATOR_PROPERTY
