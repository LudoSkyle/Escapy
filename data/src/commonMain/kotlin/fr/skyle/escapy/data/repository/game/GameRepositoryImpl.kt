package fr.skyle.escapy.data.repository.game

import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.toMilliseconds
import fr.skyle.escapy.data.document.GameDocument
import fr.skyle.escapy.data.document.MemberDocument
import fr.skyle.escapy.data.document.UserGameDocument
import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.functionCall
import fr.skyle.escapy.data.exception.withFirestoreErrors
import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.firebase.FirebaseServices
import fr.skyle.escapy.data.function.AddTimeRequest
import fr.skyle.escapy.data.function.CreateGameRequest
import fr.skyle.escapy.data.function.GameIdResponse
import fr.skyle.escapy.data.function.GameRequest
import fr.skyle.escapy.data.function.JoinGameRequest
import fr.skyle.escapy.data.function.KickPlayerRequest
import fr.skyle.escapy.data.model.StoredGame
import fr.skyle.escapy.data.model.StoredGameEntry
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

    override fun watchGamesOf(userId: String): Flow<List<StoredGameEntry>> =
        // Their own list, which the functions keep: a query on the games would keep one the user lost
        // access to, left in its cache by the Android SDK
        services().firestore.collection(USERS).document(userId).collection(USER_GAMES)
            .orderBy(CREATED_AT, Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { document -> document.data<UserGameDocument>().toStoredGameEntry(gameId = document.id) }
            }
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

    override suspend fun cancelGame(gameId: String) = callOnGame(name = CANCEL_GAME, gameId = gameId)

    override suspend fun startGame(gameId: String) = callOnGame(name = START_GAME, gameId = gameId)

    override suspend fun pauseGame(gameId: String) = callOnGame(name = PAUSE_GAME, gameId = gameId)

    override suspend fun resumeGame(gameId: String) = callOnGame(name = RESUME_GAME, gameId = gameId)

    override suspend fun addTime(gameId: String, minutes: Int, addedMinutes: Int) {
        val callable = services().functions.httpsCallable(ADD_TIME)
        functionCall { callable.invoke(AddTimeRequest(gameId = gameId, minutes = minutes, addedMinutes = addedMinutes)) }
    }

    override suspend fun endGame(gameId: String) = callOnGame(name = END_GAME, gameId = gameId)

    override suspend fun expireGame(gameId: String) = callOnGame(name = EXPIRE_GAME, gameId = gameId)

    // The answer of these calls is the game itself, which the screens already observe
    private suspend fun callOnGame(name: String, gameId: String) {
        val callable = services().functions.httpsCallable(name)
        functionCall { callable.invoke(GameRequest(gameId = gameId)) }
    }

    private companion object {
        const val GAMES = "games"
        const val PLAYERS = "players"
        const val USERS = "users"
        const val USER_GAMES = "games"
        const val CREATED_AT = "createdAt"
        const val JOINED_AT = "joinedAt"
        const val CREATE_GAME = "createGame"
        const val JOIN_GAME = "joinGame"
        const val LEAVE_GAME = "leaveGame"
        const val KICK_PLAYER = "kickPlayer"
        const val CANCEL_GAME = "cancelGame"
        const val START_GAME = "startGame"
        const val PAUSE_GAME = "pauseGame"
        const val RESUME_GAME = "resumeGame"
        const val ADD_TIME = "addTime"
        const val END_GAME = "endGame"
        const val EXPIRE_GAME = "expireGame"
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
        endsAtMs = document.endsAt?.toMilliseconds()?.toLong(),
        remainingMs = document.remainingMs,
        addedMinutes = document.addedMinutes,
        result = document.result,
        playedMs = document.playedMs,
    )
}

private fun UserGameDocument.toStoredGameEntry(gameId: String): StoredGameEntry =
    StoredGameEntry(
        gameId = gameId,
        title = title,
        isMaster = isMaster,
        masterUsername = master.username,
        masterAvatarId = master.avatarId,
        status = status,
    )

private fun MemberDocument.toStoredMember(userId: String): StoredMember =
    StoredMember(
        userId = userId,
        username = username,
        avatarId = avatarId,
    )
