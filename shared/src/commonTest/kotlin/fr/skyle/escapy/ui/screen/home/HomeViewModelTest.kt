package fr.skyle.escapy.ui.screen.home

import fr.skyle.escapy.domain.model.Session
import fr.skyle.escapy.fake.FakeSignOutUseCase
import fr.skyle.escapy.fake.FakeWatchSessionUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.screen.home.ui.HomeViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeViewModelTest : MainDispatcherTest() {

    private val watchSessionUseCase = FakeWatchSessionUseCase()
    private val signOutUseCase = FakeSignOutUseCase()

    private val viewModel = HomeViewModel(
        watchSessionUseCase = watchSessionUseCase,
        signOutUseCase = signOutUseCase,
    )

    @Test
    fun `a member signs out at once`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "member", isGuest = false))

        // When
        viewModel.onSignOutClicked()

        // Then
        assertEquals(
            expected = listOf("signOut"),
            actual = signOutUseCase.calls,
        )
    }

    @Test
    fun `a guest confirms first as they lose their account`() = runTest {
        // Given
        watchSessionUseCase.sessions.emit(Session.SignedIn(userId = "guest", isGuest = true))

        // When
        viewModel.onSignOutClicked()
        val isDialogShown = viewModel.uiState.value.isGuestSignOutDialogShown
        val callsBeforeConfirmation = signOutUseCase.calls.toList()
        viewModel.onGuestSignOutConfirmed()

        // Then
        assertTrue(isDialogShown)
        assertEquals(
            expected = emptyList(),
            actual = callsBeforeConfirmation,
        )
        assertEquals(
            expected = listOf("signOut"),
            actual = signOutUseCase.calls,
        )
    }
}
