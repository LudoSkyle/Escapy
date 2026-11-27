package fr.skyle.escapy.data.repository.clock

import fr.skyle.escapy.data.exception.FirebaseUnavailableException
import fr.skyle.escapy.data.exception.functionCall
import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.function.ServerTimeResponse
import fr.skyle.escapy.data.repository.clock.api.ServerClockRepository

internal class ServerClockRepositoryImpl(
    private val firebaseProvider: FirebaseProvider,
) : ServerClockRepository {

    override suspend fun serverNowMs(): Long {
        val services = firebaseProvider.services() ?: throw FirebaseUnavailableException()
        val callable = services.functions.httpsCallable(GET_SERVER_TIME)
        return functionCall { callable.invoke().data<ServerTimeResponse>() }.nowMs
    }

    private companion object {
        const val GET_SERVER_TIME = "getServerTime"
    }
}
