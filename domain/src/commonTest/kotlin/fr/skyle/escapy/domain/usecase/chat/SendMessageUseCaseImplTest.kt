package fr.skyle.escapy.domain.usecase.chat

import fr.skyle.escapy.data.exception.AccessDeniedException
import fr.skyle.escapy.data.model.StoredProfile
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.domain.exception.InvalidMessageException
import fr.skyle.escapy.domain.exception.MessageRefusedException
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.fake.FakeChatRepository
import fr.skyle.escapy.domain.fake.FakeProfileRepository
import fr.skyle.escapy.domain.fake.FakeSessionRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import fr.skyle.escapy.data.exception.NetworkUnavailableException as DataNetworkUnavailableException

class SendMessageUseCaseImplTest {

    private val chatRepository = FakeChatRepository()

    private val sendMessageUseCase = SendMessageUseCaseImpl(
        sessionRepository = FakeSessionRepository().apply {
            sessions = flowOf(StoredSession.SignedIn(userId = "player", isAnonymous = true, email = null, hasPassword = false))
        },
        profileRepository = FakeProfileRepository().apply {
            profiles = flowOf(StoredProfile(username = "Joueuse", avatarId = 8))
        },
        chatRepository = chatRepository,
    )

    @Test
    fun `a message goes in the name of the user with their profile without the spaces around it`() = runTest {
        // When
        sendMessageUseCase(gameId = "game", text = "  On a la clé !\n")

        // Then
        assertEquals(expected = listOf("game:player:Joueuse:8:On a la clé !"), actual = chatRepository.sent)
    }

    @Test
    fun `an empty message is refused before reaching the server`() = runTest {
        // When Then
        assertFailsWith<InvalidMessageException> { sendMessageUseCase(gameId = "game", text = "   ") }
        assertTrue(chatRepository.sent.isEmpty())
    }

    @Test
    fun `a message the rules refuse is told as refused`() = runTest {
        // Given
        chatRepository.failure = AccessDeniedException()

        // When Then
        assertFailsWith<MessageRefusedException> { sendMessageUseCase(gameId = "game", text = "Bonjour") }
    }

    @Test
    fun `a lost network is told`() = runTest {
        // Given
        chatRepository.failure = DataNetworkUnavailableException()

        // When Then
        assertFailsWith<NetworkUnavailableException> { sendMessageUseCase(gameId = "game", text = "Bonjour") }
    }
}
