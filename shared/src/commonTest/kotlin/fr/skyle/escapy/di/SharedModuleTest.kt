package fr.skyle.escapy.di

import fr.skyle.escapy.domain.usecase.mission.DeleteMissionUseCase
import fr.skyle.escapy.domain.usecase.mission.SaveMissionUseCase
import fr.skyle.escapy.domain.usecase.mission.WatchMissionsUseCase
import fr.skyle.escapy.fake.FakeDeleteMissionUseCase
import fr.skyle.escapy.fake.FakeSaveMissionUseCase
import fr.skyle.escapy.fake.FakeWatchMissionsUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.screen.editmission.ui.EditMissionViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedModuleTest : MainDispatcherTest() {

    private val koin = koinApplication {
        allowOverride(true)
        modules(
            sharedModule,
            module {
                single<WatchMissionsUseCase> { FakeWatchMissionsUseCase() }
                single<SaveMissionUseCase> { FakeSaveMissionUseCase() }
                single<DeleteMissionUseCase> { FakeDeleteMissionUseCase() }
            },
        )
    }.koin

    @Test
    fun `the mission editor opened without a mission creates one`() {
        // When
        val viewModel = koin.get<EditMissionViewModel> { parametersOf("game", null) }

        // Then
        assertTrue(viewModel.uiState.value.isNew)
    }

    @Test
    fun `the mission editor opened on a mission changes it`() {
        // When
        val viewModel = koin.get<EditMissionViewModel> { parametersOf("game", "safe") }

        // Then
        assertFalse(viewModel.uiState.value.isNew)
    }
}
