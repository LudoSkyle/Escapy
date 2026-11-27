package fr.skyle.escapy.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChatRulesTest {

    @Test
    fun `a message is sent without the spaces around it`() {
        assertEquals(expected = "On a trouvé !", actual = ChatRules.cleanMessage("  On a trouvé !\n"))
    }

    @Test
    fun `an empty message or one of spaces only is not valid`() {
        assertFalse(ChatRules.isValidMessage(""))
        assertFalse(ChatRules.isValidMessage("  \n "))
    }

    @Test
    fun `a message has 500 UTF-16 units at most as the rules count them`() {
        assertTrue(ChatRules.isValidMessage("é".repeat(500)))
        assertFalse(ChatRules.isValidMessage("é".repeat(501)))
        // An emoji counts two, as for the Firestore rules
        assertTrue(ChatRules.isValidMessage("🔑".repeat(250)))
        assertFalse(ChatRules.isValidMessage("🔑".repeat(251)))
    }
}
