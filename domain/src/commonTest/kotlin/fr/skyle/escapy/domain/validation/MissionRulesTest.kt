package fr.skyle.escapy.domain.validation

import fr.skyle.escapy.domain.model.MissionDraft
import fr.skyle.escapy.domain.model.MissionTrigger
import fr.skyle.escapy.domain.model.MissionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MissionRulesTest {

    private val code = MissionDraft(
        missionId = null,
        title = "Le coffre",
        statement = "",
        type = MissionType.CODE,
        answers = listOf("0427"),
        trigger = MissionTrigger.OnStart,
        isFinal = false,
    )

    @Test
    fun `a code has 3 to 8 digits and every code of a mission has the same length`() {
        assertTrue(MissionRules.isValid(code))
        assertTrue(MissionRules.isValid(code.copy(answers = listOf("0427", "1234"))))
        assertFalse(MissionRules.isValid(code.copy(answers = listOf("12"))))
        assertFalse(MissionRules.isValid(code.copy(answers = listOf("123456789"))))
        assertFalse(MissionRules.isValid(code.copy(answers = listOf("0427", "123"))))
        assertFalse(MissionRules.isValid(code.copy(answers = listOf("04a7"))))
    }

    @Test
    fun `a mission has a title and at least one answer`() {
        assertFalse(MissionRules.isValid(code.copy(title = "  ")))
        assertFalse(MissionRules.isValid(code.copy(answers = listOf(" "))))
        assertFalse(MissionRules.isValid(code.copy(type = MissionType.TEXT, answers = listOf("!!!"))))
        assertTrue(MissionRules.isValid(code.copy(type = MissionType.TEXT, answers = listOf("Lumière"))))
    }

    @Test
    fun `the answers are sent trimmed without the empty ones nor twice the same`() {
        assertEquals(expected = listOf("clé", "Clé"), actual = MissionRules.cleanAnswers(listOf(" clé ", "", "clé", "Clé")))
    }
}
