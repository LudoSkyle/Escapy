package fr.skyle.escapy.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals

class PasswordPolicyTest {

    @Test
    fun `a password with every rule breaks none`() {
        assertEquals(
            expected = emptySet(),
            actual = PasswordPolicy.missingRules("Escapy-2026"),
        )
    }

    @Test
    fun `an empty password breaks every rule`() {
        assertEquals(
            expected = PasswordRule.entries.toSet(),
            actual = PasswordPolicy.missingRules(""),
        )
    }

    @Test
    fun `each rule is checked on its own`() {
        assertEquals(expected = setOf(PasswordRule.MIN_LENGTH), actual = PasswordPolicy.missingRules("Es-26"))
        assertEquals(expected = setOf(PasswordRule.UPPERCASE), actual = PasswordPolicy.missingRules("escapy-2026"))
        assertEquals(expected = setOf(PasswordRule.DIGIT), actual = PasswordPolicy.missingRules("Escapy-game"))
        assertEquals(expected = setOf(PasswordRule.SPECIAL_CHARACTER), actual = PasswordPolicy.missingRules("Escapy2026"))
    }
}
