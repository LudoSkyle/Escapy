package fr.skyle.escapy.domain.usecase.chat

import fr.skyle.escapy.data.exception.AccessDeniedException
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.data.repository.chat.api.ChatRepository
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import fr.skyle.escapy.domain.mapper.toChatMessage
import fr.skyle.escapy.domain.mapper.toDomainException
import fr.skyle.escapy.domain.model.ChatMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
internal class WatchMessagesUseCaseImpl(
    private val sessionRepository: SessionRepository,
    private val chatRepository: ChatRepository,
) : WatchMessagesUseCase {

    override fun invoke(gameId: String): Flow<List<ChatMessage>> =
        sessionRepository.watchSession()
            .filterIsInstance<StoredSession.SignedIn>()
            .map { session -> session.userId }
            .distinctUntilChanged()
            .flatMapLatest { userId ->
                chatRepository.watchMessages(gameId = gameId)
                    .map { messages -> messages.map { message -> message.toChatMessage(currentUserId = userId) } }
                    // A refusal is no error here, and ends the chat where it is: the game tells the
                    // user why they lost it. Caught here, it loses none of the messages read before
                    .catch { exception -> if (exception !is AccessDeniedException) throw exception }
            }
            .catch { exception -> throw exception.toDomainException() }
}
