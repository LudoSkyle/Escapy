package fr.skyle.escapy.domain.di

import fr.skyle.escapy.data.di.dataModule
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCase
import fr.skyle.escapy.domain.usecase.session.WatchSessionUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)

    factoryOf(::WatchSessionUseCaseImpl) bind WatchSessionUseCase::class
}
