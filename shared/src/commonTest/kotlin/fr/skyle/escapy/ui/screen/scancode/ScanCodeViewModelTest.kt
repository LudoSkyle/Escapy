package fr.skyle.escapy.ui.screen.scancode

import fr.skyle.escapy.domain.exception.GameFullException
import fr.skyle.escapy.domain.exception.GameNotFoundException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.fake.FakeJoinGameUseCase
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.screen.scancode.ui.ScanCodeViewModel
import fr.skyle.escapy.ui.screen.scancode.ui.ScanCodeViewModel.ScanCodeEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScanCodeViewModelTest : MainDispatcherTest() {

    private val joinGameUseCase = FakeJoinGameUseCase()

    private val viewModel = ScanCodeViewModel(
        joinGameUseCase = joinGameUseCase,
    )

    @Test
    fun `the QR code of a game joins it and opens it`() = runTest {
        // When
        viewModel.onQrCodeRead(content = "abc234")

        // Then
        assertEquals(expected = listOf("ABC234"), actual = joinGameUseCase.calls)
        assertEquals(expected = ScanCodeEvent.OpenGame(gameId = "game"), actual = viewModel.event.first())
    }

    @Test
    fun `reading pauses as soon as a game is read so the same code joins once`() = runTest {
        // Given
        val gate = CompletableDeferred<Unit>()
        joinGameUseCase.gate = gate

        // When
        viewModel.onQrCodeRead(content = "ABC234")
        val isReadingWhileJoining = viewModel.uiState.value.isReading
        viewModel.onQrCodeRead(content = "ABC234")
        viewModel.onQrCodeRead(content = "ABC234")
        gate.complete(Unit)

        // Then
        assertFalse(isReadingWhileJoining)
        assertEquals(expected = listOf("ABC234"), actual = joinGameUseCase.calls)
    }

    @Test
    fun `a QR code of something else is told once and never sent`() = runTest {
        // When
        viewModel.onQrCodeRead(content = "https://example.com/menu")
        viewModel.onQrCodeRead(content = "https://example.com/menu")

        // Then
        assertEquals(expected = ScanCodeEvent.ShowError(error = GameError.NOT_A_GAME_QR_CODE), actual = viewModel.event.first())
        assertTrue(joinGameUseCase.calls.isEmpty())
        assertTrue(viewModel.uiState.value.isReading)
    }

    @Test
    fun `a game refused is told and its code is not sent again while it stays in front of the camera`() = runTest {
        // Given
        joinGameUseCase.failure = GameFullException()

        // When
        viewModel.onQrCodeRead(content = "ABC234")
        viewModel.onQrCodeRead(content = "ABC234")

        // Then
        assertEquals(expected = ScanCodeEvent.ShowError(error = GameError.GAME_FULL), actual = viewModel.event.first())
        assertEquals(expected = listOf("ABC234"), actual = joinGameUseCase.calls)
        assertTrue(viewModel.uiState.value.isReading)
    }

    @Test
    fun `a game refused for good is not sent again even after a while`() = runTest {
        // Given
        joinGameUseCase.failure = GameNotFoundException()
        viewModel.onQrCodeRead(content = "ABC234")

        // When
        advanceTimeBy(delayTimeMillis = 60_000)
        viewModel.onQrCodeRead(content = "ABC234")

        // Then
        assertEquals(expected = listOf("ABC234"), actual = joinGameUseCase.calls)
    }

    @Test
    fun `a QR code of something else is told once even after a while`() = runTest {
        // Given
        viewModel.onQrCodeRead(content = "https://example.com/menu")
        viewModel.event.first()

        // When
        advanceTimeBy(delayTimeMillis = 60_000)
        viewModel.onQrCodeRead(content = "https://example.com/menu")
        viewModel.onQrCodeRead(content = "ABC234")

        // Then: no second error comes before the game opens
        assertEquals(expected = ScanCodeEvent.OpenGame(gameId = "game"), actual = viewModel.event.first())
    }

    @Test
    fun `a code that failed on the network can be tried again after a while`() = runTest {
        // Given
        joinGameUseCase.failure = NetworkUnavailableException()
        viewModel.onQrCodeRead(content = "ABC234")
        joinGameUseCase.failure = null

        // When
        viewModel.onQrCodeRead(content = "ABC234")
        val callsAtOnce = joinGameUseCase.calls.size
        advanceTimeBy(delayTimeMillis = 6_000)
        viewModel.onQrCodeRead(content = "ABC234")

        // Then
        assertEquals(expected = 1, actual = callsAtOnce)
        assertEquals(expected = listOf("ABC234", "ABC234"), actual = joinGameUseCase.calls)
    }

    @Test
    fun `a device without a camera is told`() = runTest {
        // When
        viewModel.onCameraUnavailable()

        // Then
        assertTrue(viewModel.uiState.value.isCameraUnavailable)
    }
}
