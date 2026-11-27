package fr.skyle.escapy.domain.usecase.clock

import fr.skyle.escapy.data.repository.clock.api.ServerClockRepository
import fr.skyle.escapy.domain.mapper.toDomainException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.TimeSource

/**
 * Asks the server its time a few times. The server read its clock somewhere during the round trip,
 * halfway on average: the shorter the trip, the closer the estimate. The first call often wakes the
 * function up and comes back slowly; the next ones are kept when faster.
 */
internal class EstimateServerClockOffsetUseCaseImpl(
    private val serverClockRepository: ServerClockRepository,
    private val clock: Clock,
    // Monotonic: a round trip is measured by a clock nobody sets during it
    private val timeSource: TimeSource,
) : EstimateServerClockOffsetUseCase {

    override fun invoke(): Flow<Duration> = flow {
        var fastestRoundTrip: Duration? = null
        var lastFailure: Exception? = null
        repeat(SAMPLES) {
            try {
                val sent = timeSource.markNow()
                val serverNow = Instant.fromEpochMilliseconds(serverClockRepository.serverNowMs())
                val roundTrip = sent.elapsedNow()
                val deviceNow = clock.now()
                if (fastestRoundTrip == null || roundTrip < fastestRoundTrip!!) {
                    fastestRoundTrip = roundTrip
                    emit(serverNow + roundTrip / 2 - deviceNow)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                lastFailure = exception
            }
        }
        if (fastestRoundTrip == null) {
            throw (lastFailure ?: IllegalStateException("No answer from the server")).toDomainException()
        }
    }

    private companion object {
        const val SAMPLES = 3
    }
}
