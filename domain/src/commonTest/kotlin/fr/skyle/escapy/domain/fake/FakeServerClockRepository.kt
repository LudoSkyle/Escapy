package fr.skyle.escapy.domain.fake

import fr.skyle.escapy.data.exception.NetworkUnavailableException
import fr.skyle.escapy.data.repository.clock.api.ServerClockRepository
import kotlin.time.Duration
import kotlin.time.TestTimeSource

/**
 * A server whose answers take the round trips given, one per call, on [timeSource]: it reads its
 * clock halfway, [serverAheadMs] ahead of the device clock, which starts at [deviceStartMs].
 */
class FakeServerClockRepository(
    private val timeSource: TestTimeSource,
    private val deviceStartMs: Long,
    private val serverAheadMs: Long,
    private val roundTrips: List<Duration?>,
) : ServerClockRepository {

    private var calls = 0

    /** The time of the device now, as a clock reading [timeSource] tells it. */
    val deviceNowMs: Long
        get() = deviceStartMs + elapsed.inWholeMilliseconds

    private var elapsed = Duration.ZERO

    override suspend fun serverNowMs(): Long {
        // Null: this call fails
        val roundTrip = roundTrips[calls++] ?: throw NetworkUnavailableException()
        advance(roundTrip / 2)
        val serverNow = deviceNowMs + serverAheadMs
        advance(roundTrip / 2)
        return serverNow
    }

    private fun advance(duration: Duration) {
        timeSource += duration
        elapsed += duration
    }
}
