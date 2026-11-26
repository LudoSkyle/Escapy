package fr.skyle.escapy.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameRulesTest {

    @Test
    fun `a code is read without its spaces in capitals`() {
        assertEquals(expected = "ABC234", actual = GameRules.cleanJoinCode(" abc 23 4\n"))
    }

    @Test
    fun `a code is complete with its six characters spaces apart`() {
        assertTrue(GameRules.isJoinCodeComplete("abc 234"))
        assertFalse(GameRules.isJoinCodeComplete("ABC23"))
        assertFalse(GameRules.isJoinCodeComplete("ABC2345"))
    }
}
