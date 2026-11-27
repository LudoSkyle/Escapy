package fr.skyle.escapy.domain.usecase.chat

import fr.skyle.escapy.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * The latest messages of the chat of a game, the oldest first, then each change; a message the user
 * sends shows at once. Ends once the user can no longer read the game, such as a player excluded:
 * the game itself tells why (see WatchGameUseCase).
 */
interface WatchMessagesUseCase {
    operator fun invoke(gameId: String): Flow<List<ChatMessage>>
}
