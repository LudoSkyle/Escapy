package fr.skyle.escapy.data.repository.chat.api

import fr.skyle.escapy.data.model.StoredMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    /**
     * The latest messages of the chat of a game, the oldest first, then each change, a message sent
     * from this device included at once. Throws AccessDeniedException once the user is no longer a
     * member.
     */
    fun watchMessages(gameId: String): Flow<List<StoredMessage>>

    /**
     * Sends a message in the name of the signed-in user, with the name and avatar of their profile,
     * which the rules check. Returns once the server acknowledged it; throws AccessDeniedException
     * when the rules refuse it (a game over, a user excluded).
     */
    suspend fun sendMessage(gameId: String, authorId: String, username: String, avatarId: Int, text: String)
}
