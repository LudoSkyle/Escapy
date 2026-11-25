package fr.skyle.escapy.domain.di

import fr.skyle.escapy.data.di.FIREBASE_EMULATOR_PROPERTY as DATA_FIREBASE_EMULATOR_PROPERTY
import fr.skyle.escapy.data.di.dataModule
import fr.skyle.escapy.domain.usecase.account.ChangeEmailUseCase
import fr.skyle.escapy.domain.usecase.account.ChangeEmailUseCaseImpl
import fr.skyle.escapy.domain.usecase.account.ChangePasswordUseCase
import fr.skyle.escapy.domain.usecase.account.ChangePasswordUseCaseImpl
import fr.skyle.escapy.domain.usecase.account.DeleteAccountUseCase
import fr.skyle.escapy.domain.usecase.account.DeleteAccountUseCaseImpl
import fr.skyle.escapy.domain.usecase.account.LinkGuestAccountUseCase
import fr.skyle.escapy.domain.usecase.account.LinkGuestAccountUseCaseImpl
import fr.skyle.escapy.domain.usecase.account.LinkGuestWithGoogleUseCase
import fr.skyle.escapy.domain.usecase.account.LinkGuestWithGoogleUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SendPasswordResetUseCase
import fr.skyle.escapy.domain.usecase.auth.SendPasswordResetUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SignInAsGuestUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInAsGuestUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SignInWithEmailUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInWithEmailUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SignInWithGoogleUseCase
import fr.skyle.escapy.domain.usecase.auth.SignInWithGoogleUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SignOutUseCase
import fr.skyle.escapy.domain.usecase.auth.SignOutUseCaseImpl
import fr.skyle.escapy.domain.usecase.auth.SignUpWithEmailUseCase
import fr.skyle.escapy.domain.usecase.auth.SignUpWithEmailUseCaseImpl
import fr.skyle.escapy.domain.usecase.profile.EnsureProfileUseCase
import fr.skyle.escapy.domain.usecase.profile.EnsureProfileUseCaseImpl
import fr.skyle.escapy.domain.usecase.profile.UpdateAvatarUseCase
import fr.skyle.escapy.domain.usecase.profile.UpdateAvatarUseCaseImpl
import fr.skyle.escapy.domain.usecase.profile.UpdateUsernameUseCase
import fr.skyle.escapy.domain.usecase.profile.UpdateUsernameUseCaseImpl
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
    factoryOf(::SignInWithGoogleUseCaseImpl) bind SignInWithGoogleUseCase::class
    factoryOf(::SignInAsGuestUseCaseImpl) bind SignInAsGuestUseCase::class
    factoryOf(::SendPasswordResetUseCaseImpl) bind SendPasswordResetUseCase::class
    factoryOf(::SignOutUseCaseImpl) bind SignOutUseCase::class

    // Profile
    factoryOf(::WatchProfileUseCaseImpl) bind WatchProfileUseCase::class
    factoryOf(::EnsureProfileUseCaseImpl) bind EnsureProfileUseCase::class
    factoryOf(::UpdateUsernameUseCaseImpl) bind UpdateUsernameUseCase::class
    factoryOf(::UpdateAvatarUseCaseImpl) bind UpdateAvatarUseCase::class

    // Account
    factoryOf(::LinkGuestAccountUseCaseImpl) bind LinkGuestAccountUseCase::class
    factoryOf(::LinkGuestWithGoogleUseCaseImpl) bind LinkGuestWithGoogleUseCase::class
    factoryOf(::ChangeEmailUseCaseImpl) bind ChangeEmailUseCase::class
    factoryOf(::ChangePasswordUseCaseImpl) bind ChangePasswordUseCase::class
    factoryOf(::DeleteAccountUseCaseImpl) bind DeleteAccountUseCase::class
}

/** The Koin property a debug host sets to "true" to use the Firebase emulators. */
const val FIREBASE_EMULATOR_PROPERTY = DATA_FIREBASE_EMULATOR_PROPERTY
