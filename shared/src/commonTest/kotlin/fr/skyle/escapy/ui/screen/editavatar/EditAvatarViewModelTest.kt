package fr.skyle.escapy.ui.screen.editavatar

import fr.skyle.escapy.domain.model.Profile
import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.fake.FakeUpdateAvatarUseCase
import fr.skyle.escapy.fake.FakeWatchProfileUseCase
import fr.skyle.escapy.fake.FakeWatchSessionUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.screen.editavatar.ui.EditAvatarViewModel
import fr.skyle.escapy.ui.screen.editavatar.ui.EditAvatarViewModel.EditAvatarEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EditAvatarViewModelTest : MainDispatcherTest() {

    private val watchSessionUseCase = FakeWatchSessionUseCase()
    private val watchProfileUseCase = FakeWatchProfileUseCase()
    private val updateAvatarUseCase = FakeUpdateAvatarUseCase()

    private val viewModel = EditAvatarViewModel(
        watchSessionUseCase = watchSessionUseCase,
        watchProfileUseCase = watchProfileUseCase,
        updateAvatarUseCase = updateAvatarUseCase,
    )

    @Test
    fun `the current avatar is the chosen one`() = runTest {
        // When
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "user", isGuest = false))
        watchProfileUseCase.profiles.emit(Profile(username = "Player_ABC234", avatarId = 7))

        // Then
        assertEquals(
            expected = 7,
            actual = viewModel.uiState.value.currentAvatarId,
        )
    }

    @Test
    fun `a new avatar is saved once then the screen closes`() = runTest {
        // Given
        updateAvatarUseCase.gate = CompletableDeferred()

        // When
        viewModel.onAvatarSelected(42)
        viewModel.onAvatarSelected(12)
        val saving = viewModel.uiState.value.savingAvatarId
        updateAvatarUseCase.gate?.complete(Unit)

        // Then
        assertEquals(
            expected = 42,
            actual = saving,
        )
        assertEquals(
            expected = listOf("42"),
            actual = updateAvatarUseCase.calls,
        )
        assertEquals(
            expected = EditAvatarEvent.Saved,
            actual = viewModel.event.first(),
        )
    }
}
