package fr.skyle.escapy.ui.core.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class GameTextsTest {

    @Test
    fun `a timer shows minutes and seconds and the hours once there are some`() {
        assertEquals(expected = "04:59", actual = timerText(duration = 4.minutes + 59.seconds))
        assertEquals(expected = "00:00", actual = timerText(duration = Duration.ZERO))
        assertEquals(expected = "1:02:03", actual = timerText(duration = 1.hours + 2.minutes + 3.seconds))
    }

    @Test
    fun `a part of a second counts as a whole one`() {
        assertEquals(expected = "00:01", actual = timerText(duration = 1.milliseconds))
        assertEquals(expected = "01:00", actual = timerText(duration = 59.seconds + 10.milliseconds))
    }
}
