package fr.skyle.escapy.data.usecase.lobby

import com.google.firebase.auth.FirebaseAuth
import fr.skyle.escapy.data.ext.isUserInLobby
import fr.skyle.escapy.data.utils.Either
import fr.skyle.escapy.data.utils.EitherFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

interface EnsureLobbyAccessibleUseCase {

    suspend operator fun invoke(lobbyId: String): Either<LobbyAccessResult.Failure, LobbyAccessResult.Success>
}

class EnsureLobbyAccessibleUseCaseImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val watchLobbyUseCase: WatchLobbyUseCase,
) : EnsureLobbyAccessibleUseCase {

    override suspend fun invoke(lobbyId: String): Either<LobbyAccessResult.Failure, LobbyAccessResult.Success> {
        return try {
            val lobby = watchLobbyUseCase(lobbyId).firstOrNull()
                ?: return Either.Failure(LobbyAccessResult.Failure.NotFound)

            val user = firebaseAuth.currentUser
                ?: return Either.Failure(LobbyAccessResult.Failure.NoCurrentUser)

            if (!lobby.isUserInLobby(user.uid)) {
                return Either.Failure(LobbyAccessResult.Failure.NotInLobby)
            }

            Either.Success(LobbyAccessResult.Success)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Either.Failure(LobbyAccessResult.Failure.Unexpected(e))
        }
    }
}

class LobbyAccessException(val failure: LobbyAccessResult.Failure) : Exception()

sealed interface LobbyAccessResult {
    data object Success : LobbyAccessResult

    sealed interface Failure : LobbyAccessResult, EitherFailure {
        data object NotFound : Failure
        data object NotInLobby : Failure
        data object NoCurrentUser : Failure
        data class Unexpected(val cause: Throwable) : Failure

        override fun toException(): Exception =
            LobbyAccessException(failure = this)
        }
    }
}