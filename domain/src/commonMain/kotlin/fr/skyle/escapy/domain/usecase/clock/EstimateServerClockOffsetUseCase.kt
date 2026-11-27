package fr.skyle.escapy.domain.usecase.clock

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * How far the clock of the server is ahead of the clock of the device: the time of the server is the
 * time of the device plus this offset. The timer of a game is shown from it, never from the clock of
 * the device alone (see docs/Common/02_CONVENTIONS.md).
 *
 * Emits a first estimate as soon as the server answers once, then a better one each time an answer
 * comes back faster, and completes. Throws when no answer comes at all.
 */
interface EstimateServerClockOffsetUseCase {
    operator fun invoke(): Flow<Duration>
}
