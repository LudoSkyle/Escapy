package fr.skyle.escapy.ui.screen.chat

import fr.skyle.escapy.domain.exception.MessageRefusedException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.model.GameState
import fr.skyle.escapy.domain.model.GameStatus
import fr.skyle.escapy.fake.FakeExpireGameUseCase
import fr.skyle.escapy.fake.FakeSendMessageUseCase
import fr.skyle.escapy.fake.FakeWatchGameUseCase
import fr.skyle.escapy.fake.FakeWatchMessagesUseCase
import fr.skyle.escapy.fake.chatMessage
import fr.skyle.escapy.fake.game
import fr.skyle.escapy.test.MainDispatcherTest
import fr.skyle.escapy.ui.core.error.GameError
import fr.skyle.escapy.ui.screen.chat.ui.ChatViewModel
import fr.skyle.escapy.ui.screen.chat.ui.ChatViewModel.ChatEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChatViewModelTest : MainDispatcherTest() {

    private val watchGameUseCase = FakeWatchGameUseCase()
    private val watchMessagesUseCase = FakeWatchMessagesUseCase()
    private val sendMessageUseCase = FakeSendMessageUseCase()
    private val expireGameUseCase = FakeExpireGameUseCase()

    private val viewModel = ChatViewModel(
        gameId = "game",
        watchGameUseCase = watchGameUseCase,
        watchMessagesUseCase = watchMessagesUseCase,
        sendMessageUseCase = sendMessageUseCase,
        expireGameUseCase = expireGameUseCase,
    )

    @Test
    fun `the messages are shown live`() = runTest {
        // Given
        val messages = listOf(chatMessage(id = "1"), chatMessage(id = "2", isMine = true))

        // When
        watchMessagesUseCase.messages.emit(messages)

        // Then
        assertEquals(expected = messages, actual = viewModel.uiState.value.messages)
    }

    @Test
    fun `the user writes while the game is played and no longer once it is over`() = runTest {
        // When
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = false, status = GameStatus.PAUSED), players = emptyList()))
        val isOpenWhilePaused = viewModel.uiState.value.isOpen
        watchGameUseCase.states.emit(GameState.Available(game = game(isMaster = false, status = GameStatus.FINISHED), players = emptyList()))

        // Then
        assertTrue(isOpenWhilePaused)
        assertFalse(viewModel.uiState.value.isOpen)
    }

    @Test
    fun `a message is sent without waiting for the server to acknowledge it`() = runTest {
        // Given: the server takes its time
        sendMessageUseCase.gate = CompletableDeferred()

        // When
        viewModel.onSendClicked(text = "On a la clé !")

        // Then: the call is under way, and nothing tells the screen to wait
        assertEquals(expected = listOf("game:On a la clé !"), actual = sendMessageUseCase.calls)
    }

    @Test
    fun `a message of spaces only is not sent`() = runTest {
        // When
        viewModel.onSendClicked(text = "   ")

        // Then
        assertTrue(sendMessageUseCase.calls.isEmpty())
    }

    @Test
    fun `a message refused comes back with the reason and the server is asked whether the time is up`() = runTest {
        // Given
        sendMessageUseCase.failure = MessageRefusedException()

        // When
        viewModel.onSendClicked(text = "Trop tard ?")

        // Then
        assertEquals(
            expected = ChatEvent.SendFailed(text = "Trop tard ?", error = GameError.MESSAGE_REFUSED),
            actual = viewModel.event.first(),
        )
        assertEquals(expected = listOf("game"), actual = expireGameUseCase.calls)
    }

    @Test
    fun `a message lost on the network comes back without asking the server about the time`() = runTest {
        // Given
        sendMessageUseCase.failure = NetworkUnavailableException()

        // When
        viewModel.onSendClicked(text = "Allô ?")

        // Then
        assertEquals(expected = ChatEvent.SendFailed(text = "Allô ?", error = GameError.NETWORK), actual = viewModel.event.first())
        assertTrue(expireGameUseCase.calls.isEmpty())
    }
}
