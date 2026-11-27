package fr.skyle.escapy.data.repository.chat

import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.ServerTimestampBehavior
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.toMilliseconds
import fr.skyle.escapy.data.document.MemberDocument
import fr.skyle.escapy.data.document.MessageDocument
import fr.skyle.escapy.data.document.NewMessageDocument
import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.firestoreWrite
import fr.skyle.escapy.data.exception.withFirestoreErrors
import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.firebase.FirebaseServices
import fr.skyle.escapy.data.model.StoredMessage
import fr.skyle.escapy.data.repository.chat.api.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class ChatRepositoryImpl(
    private val firebaseProvider: FirebaseProvider,
) : ChatRepository {

    private fun services(): FirebaseServices =
        firebaseProvider.services() ?: throw FirebaseUnavailableException()

    private fun messages(gameId: String) =
        services().firestore.collection(GAMES).document(gameId).collection(MESSAGES)

    override fun watchMessages(gameId: String): Flow<List<StoredMessage>> =
        // The latest first, to keep only them, then put back in order; the metadata tell a message
        // still waiting for the server
        messages(gameId)
            .orderBy(SENT_AT, Direction.DESCENDING)
            .limit(MESSAGES_SHOWN)
            .snapshots(includeMetadataChanges = true)
            .map { snapshot ->
                snapshot.documents.reversed().map { document ->
                    val message = document.data<MessageDocument>(serverTimestampBehavior = ServerTimestampBehavior.ESTIMATE)
                    StoredMessage(
                        id = document.id,
                        authorId = message.authorId,
                        authorUsername = message.author.username,
                        authorAvatarId = message.author.avatarId,
                        text = message.text,
                        sentAtMs = message.sentAt?.toMilliseconds()?.toLong(),
                        isPending = document.metadata.hasPendingWrites,
                    )
                }
            }
            .distinctUntilChanged()
            .withFirestoreErrors()

    override suspend fun sendMessage(gameId: String, authorId: String, username: String, avatarId: Int, text: String) {
        val message = NewMessageDocument(
            authorId = authorId,
            author = MemberDocument(username = username, avatarId = avatarId),
            text = text,
            sentAt = Timestamp.ServerTimestamp,
        )
        firestoreWrite { messages(gameId).add(message) }
    }

    private companion object {
        const val GAMES = "games"
        const val MESSAGES = "messages"
        const val SENT_AT = "sentAt"
        const val MESSAGES_SHOWN = 200
    }
}
