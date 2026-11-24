package fr.skyle.escapy.di

import fr.skyle.escapy.domain.di.domainModule
import fr.skyle.escapy.ui.main.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    includes(domainModule)

    viewModelOf(::MainViewModel)
}
