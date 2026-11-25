package fr.skyle.escapy.di

import fr.skyle.escapy.domain.di.domainModule
import fr.skyle.escapy.ui.main.MainViewModel
import fr.skyle.escapy.ui.screen.forgotpassword.ui.ForgotPasswordViewModel
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel
import fr.skyle.escapy.ui.screen.signin.ui.SignInViewModel
import fr.skyle.escapy.ui.screen.signup.ui.SignUpViewModel
import fr.skyle.escapy.ui.screen.welcome.ui.WelcomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    includes(domainModule)

    viewModelOf(::MainViewModel)
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::SignInViewModel)
    viewModelOf(::SignUpViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::HomeViewModel)
}
