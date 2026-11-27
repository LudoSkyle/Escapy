package fr.skyle.escapy.domain.fake

import fr.skyle.escapy.data.model.StoredMessage
import fr.skyle.escapy.data.repository.chat.api.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeChatRepository : ChatRepository {

    var messages: Flow<List<StoredMessage>> = emptyFlow()

    /** Thrown by the next sends when set. */
    var failure: Exception? = null

    val sent = mutableListOf<String>()

    override fun watchMessages(gameId: String): Flow<List<StoredMessage>> = messages

    override suspend fun sendMessage(gameId: String, authorId: String, username: String, avatarId: Int, text: String) {
        sent += "$gameId:$authorId:$username:$avatarId:$text"
        failure?.let { exception -> throw exception }
    }
}

fun storedMessage(id: String, authorId: String, sentAtMs: Long? = 1_000, isPending: Boolean = false): StoredMessage =
    StoredMessage(
        id = id,
        authorId = authorId,
        authorUsername = "Name of $authorId",
        authorAvatarId = 4,
        text = "Message $id",
        sentAtMs = sentAtMs,
        isPending = isPending,
    )
