package fr.skyle.escapy.di

import fr.skyle.escapy.domain.di.domainModule
import fr.skyle.escapy.ui.main.MainViewModel
import fr.skyle.escapy.ui.screen.about.ui.AboutViewModel
import fr.skyle.escapy.ui.screen.changeemail.ui.ChangeEmailViewModel
import fr.skyle.escapy.ui.screen.changepassword.ui.ChangePasswordViewModel
import fr.skyle.escapy.ui.screen.creategame.ui.CreateGameViewModel
import fr.skyle.escapy.ui.screen.deleteaccount.ui.DeleteAccountViewModel
import fr.skyle.escapy.ui.screen.editavatar.ui.EditAvatarViewModel
import fr.skyle.escapy.ui.screen.editusername.ui.EditUsernameViewModel
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel
import fr.skyle.escapy.ui.screen.linkaccount.ui.LinkAccountViewModel
import fr.skyle.escapy.ui.screen.profile.ui.ProfileViewModel
import fr.skyle.escapy.ui.screen.forgotpassword.ui.ForgotPasswordViewModel
import fr.skyle.escapy.ui.screen.scancode.ui.ScanCodeViewModel
import fr.skyle.escapy.ui.screen.signin.ui.SignInViewModel
import fr.skyle.escapy.ui.screen.signup.ui.SignUpViewModel
import fr.skyle.escapy.ui.screen.play.ui.PlayViewModel
import fr.skyle.escapy.ui.screen.waitingroom.ui.WaitingRoomViewModel
import fr.skyle.escapy.ui.screen.welcome.ui.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    includes(domainModule)

    viewModelOf(::MainViewModel)
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::SignInViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::EditUsernameViewModel)
    viewModelOf(::EditAvatarViewModel)
    viewModelOf(::LinkAccountViewModel)
    viewModelOf(::ChangeEmailViewModel)
    viewModelOf(::ChangePasswordViewModel)
    viewModelOf(::DeleteAccountViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::CreateGameViewModel)
    viewModelOf(::ScanCodeViewModel)
    // The game the screen opened, given by its route
    viewModel { parameters ->
        WaitingRoomViewModel(
            gameId = parameters.get(),
            watchGameUseCase = get(),
            leaveGameUseCase = get(),
            kickPlayerUseCase = get(),
            cancelGameUseCase = get(),
            startGameUseCase = get(),
        )
    }
    viewModel { parameters ->
        PlayViewModel(
            gameId = parameters.get(),
            watchGameUseCase = get(),
            estimateServerClockOffsetUseCase = get(),
            pauseGameUseCase = get(),
            resumeGameUseCase = get(),
            addTimeUseCase = get(),
            endGameUseCase = get(),
            expireGameUseCase = get(),
            kickPlayerUseCase = get(),
            leaveGameUseCase = get(),
            cancelGameUseCase = get(),
            clock = get(),
        )
    }
    // The version the host gave when it started
    viewModel { AboutViewModel(appVersion = getProperty(APP_VERSION_PROPERTY)) }
}
