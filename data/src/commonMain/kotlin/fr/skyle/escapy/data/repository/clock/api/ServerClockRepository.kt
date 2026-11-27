package fr.skyle.escapy.data.repository.clock.api

interface ServerClockRepository {

    /** The time of the server, in milliseconds since the epoch, as it answered. */
    suspend fun serverNowMs(): Long
}
