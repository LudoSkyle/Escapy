package fr.skyle.escapy.data.repository.game

import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot
import fr.skyle.escapy.data.document.GameDocument
import fr.skyle.escapy.data.document.MemberDocument
import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.functionCall
import fr.skyle.escapy.data.exception.withFirestoreErrors
import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.firebase.FirebaseServices
import fr.skyle.escapy.data.function.CreateGameRequest
import fr.skyle.escapy.data.function.GameIdResponse
import fr.skyle.escapy.data.function.GameRequest
import fr.skyle.escapy.data.function.JoinGameRequest
import fr.skyle.escapy.data.function.KickPlayerRequest
import fr.skyle.escapy.data.model.StoredGame
import fr.skyle.escapy.data.model.StoredMember
import fr.skyle.escapy.data.repository.game.api.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class GameRepositoryImpl(
    private val firebaseProvider: FirebaseProvider,
) : GameRepository {

    private fun services(): FirebaseServices =
        firebaseProvider.services() ?: throw FirebaseUnavailableException()

    override fun watchGamesOf(userId: String): Flow<List<StoredGame>> =
        // The query the rules accept: a user lists the games they are a member of only
        services().firestore.collection(GAMES)
            .where { MEMBER_IDS contains userId }
            .orderBy(CREATED_AT, Direction.DESCENDING)
            .snapshots
            .map { snapshot -> snapshot.documents.map { document -> document.toStoredGame() } }
            .distinctUntilChanged()
            .withFirestoreErrors()

    override fun watchGame(gameId: String): Flow<StoredGame?> =
        services().firestore.collection(GAMES).document(gameId).snapshots
            .map { snapshot -> if (snapshot.exists) snapshot.toStoredGame() else null }
            .distinctUntilChanged()
            .withFirestoreErrors()

    override fun watchPlayers(gameId: String): Flow<List<StoredMember>> =
        services().firestore.collection(GAMES).document(gameId).collection(PLAYERS)
            .orderBy(JOINED_AT)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { document ->
                    document.data<MemberDocument>().toStoredMember(userId = document.id)
                }
            }
            .distinctUntilChanged()
            .withFirestoreErrors()

    override suspend fun createGame(title: String, durationMinutes: Int, maxPlayers: Int?): String {
        val callable = services().functions.httpsCallable(CREATE_GAME)
        val request = CreateGameRequest(title = title, durationMinutes = durationMinutes, maxPlayers = maxPlayers)
        return functionCall { callable.invoke(request).data<GameIdResponse>() }.gameId
    }

    override suspend fun joinGame(code: String): String {
        val callable = services().functions.httpsCallable(JOIN_GAME)
        return functionCall { callable.invoke(JoinGameRequest(code = code)).data<GameIdResponse>() }.gameId
    }

    override suspend fun leaveGame(gameId: String) {
        val callable = services().functions.httpsCallable(LEAVE_GAME)
        functionCall { callable.invoke(GameRequest(gameId = gameId)) }
    }

    override suspend fun kickPlayer(gameId: String, playerId: String) {
        val callable = services().functions.httpsCallable(KICK_PLAYER)
        functionCall { callable.invoke(KickPlayerRequest(gameId = gameId, playerId = playerId)) }
    }

    override suspend fun cancelGame(gameId: String) {
        val callable = services().functions.httpsCallable(CANCEL_GAME)
        functionCall { callable.invoke(GameRequest(gameId = gameId)) }
    }

    private companion object {
        const val GAMES = "games"
        const val PLAYERS = "players"
        const val MEMBER_IDS = "memberIds"
        const val CREATED_AT = "createdAt"
        const val JOINED_AT = "joinedAt"
        const val CREATE_GAME = "createGame"
        const val JOIN_GAME = "joinGame"
        const val LEAVE_GAME = "leaveGame"
        const val KICK_PLAYER = "kickPlayer"
        const val CANCEL_GAME = "cancelGame"
    }
}

private fun DocumentSnapshot.toStoredGame(): StoredGame {
    val document = data<GameDocument>()
    return StoredGame(
        id = id,
        title = document.title,
        durationMinutes = document.durationMinutes,
        maxPlayers = document.maxPlayers,
        status = document.status,
        masterId = document.masterId,
        master = document.master.toStoredMember(userId = document.masterId),
        playerCount = document.playerCount,
        joinCode = document.joinCode,
    )
}

private fun MemberDocument.toStoredMember(userId: String): StoredMember =
    StoredMember(
        userId = userId,
        username = username,
        avatarId = avatarId,
    )
