package fr.skyle.escapy.domain.usecase.clock

import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.fake.FakeServerClockRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TestTimeSource

class EstimateServerClockOffsetUseCaseImplTest {

    private val timeSource = TestTimeSource()

    private fun useCase(serverAheadMs: Long, roundTrips: List<Duration?>): EstimateServerClockOffsetUseCaseImpl {
        val server = FakeServerClockRepository(
            timeSource = timeSource,
            deviceStartMs = 1_000_000,
            serverAheadMs = serverAheadMs,
            roundTrips = roundTrips,
        )
        return EstimateServerClockOffsetUseCaseImpl(
            serverClockRepository = server,
            clock = object : Clock {
                override fun now(): Instant = Instant.fromEpochMilliseconds(server.deviceNowMs)
            },
            timeSource = timeSource,
        )
    }

    @Test
    fun `the offset is told at once then refined by faster answers only`() = runTest {
        // Given: a slow first answer, the function waking up
        val estimate = useCase(serverAheadMs = 19_000, roundTrips = listOf(2.seconds, 200.milliseconds, 400.milliseconds))

        // When
        val offsets = estimate().toList()

        // Then: an answer read halfway is exact, whatever its round trip
        assertEquals(expected = listOf(19.seconds, 19.seconds), actual = offsets)
    }

    @Test
    fun `a server behind the device gives a negative offset`() = runTest {
        // Given
        val estimate = useCase(serverAheadMs = -3_000, roundTrips = listOf(100.milliseconds, 300.milliseconds, 300.milliseconds))

        // When
        val offsets = estimate().toList()

        // Then
        assertEquals(expected = listOf((-3).seconds), actual = offsets)
    }

    @Test
    fun `a failed answer is skipped`() = runTest {
        // Given
        val estimate = useCase(serverAheadMs = 5_000, roundTrips = listOf(null, 100.milliseconds, null))

        // When
        val offsets = estimate().toList()

        // Then
        assertEquals(expected = listOf(5.seconds), actual = offsets)
    }

    @Test
    fun `no answer at all is told`() = runTest {
        // Given
        val estimate = useCase(serverAheadMs = 5_000, roundTrips = listOf(null, null, null))

        // When Then
        assertFailsWith<NetworkUnavailableException> { estimate().toList() }
    }
}
