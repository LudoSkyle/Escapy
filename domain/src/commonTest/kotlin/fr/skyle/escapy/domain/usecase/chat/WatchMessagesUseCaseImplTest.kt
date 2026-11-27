package fr.skyle.escapy.domain.usecase.chat

import fr.skyle.escapy.data.exception.AccessDeniedException
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.domain.fake.FakeChatRepository
import fr.skyle.escapy.domain.fake.FakeSessionRepository
import fr.skyle.escapy.domain.fake.storedMessage
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class WatchMessagesUseCaseImplTest {

    private val sessionRepository = FakeSessionRepository().apply {
        sessions = flowOf(StoredSession.SignedIn(userId = "player", isAnonymous = true, email = null, hasPassword = false))
    }
    private val chatRepository = FakeChatRepository()

    private val watchMessagesUseCase = WatchMessagesUseCaseImpl(
        sessionRepository = sessionRepository,
        chatRepository = chatRepository,
    )

    @Test
    fun `the messages tell which ones the user wrote and which wait for the server`() = runTest {
        // Given
        chatRepository.messages = flowOf(
            listOf(storedMessage(id = "1", authorId = "master"), storedMessage(id = "2", authorId = "player", sentAtMs = null, isPending = true)),
        )

        // When
        val messages = watchMessagesUseCase(gameId = "game").toList().single()

        // Then
        assertFalse(messages[0].isMine)
        assertEquals(expected = Instant.fromEpochMilliseconds(1_000), actual = messages[0].sentAt)
        assertTrue(messages[1].isMine)
        assertTrue(messages[1].isPending)
        assertNull(messages[1].sentAt)
    }

    @Test
    fun `a chat that can no longer be read ends without an error`() = runTest {
        // Given
        chatRepository.messages = flow {
            emit(listOf(storedMessage(id = "1", authorId = "master")))
            throw AccessDeniedException()
        }

        // When
        val emissions = watchMessagesUseCase(gameId = "game").toList()

        // Then
        assertEquals(expected = 1, actual = emissions.size)
    }
}
