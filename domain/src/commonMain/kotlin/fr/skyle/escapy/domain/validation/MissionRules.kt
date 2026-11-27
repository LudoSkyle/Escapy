package fr.skyle.escapy.domain.validation

import fr.skyle.escapy.domain.model.MissionDraft
import fr.skyle.escapy.domain.model.MissionType

/**
 * The rules of a mission, the same the server checks (functions/src/game/mission.ts): the app checks
 * them to help the game master, the server decides.
 */
object MissionRules {

    const val TITLE_MAX_LENGTH = 60
    const val STATEMENT_MAX_LENGTH = 1000
    const val ANSWERS_MAX = 10
    const val TEXT_ANSWER_MAX_LENGTH = 100
    const val CODE_LENGTH_MIN = 3
    const val CODE_LENGTH_MAX = 8
    const val MISSIONS_PER_GAME_LIMIT = 30

    fun isValidTitle(title: String): Boolean = title.trim().length in 1..TITLE_MAX_LENGTH

    fun isValidCode(code: String): Boolean =
        code.length in CODE_LENGTH_MIN..CODE_LENGTH_MAX && code.all { character -> character in '0'..'9' }

    fun isValidTextAnswer(answer: String): Boolean =
        answer.trim().length in 1..TEXT_ANSWER_MAX_LENGTH && answer.any { character -> character.isLetterOrDigit() }

    /** The answers as sent: trimmed, without the empty ones nor the same one twice. */
    fun cleanAnswers(answers: List<String>): List<String> =
        answers.map { answer -> answer.trim() }.filter { answer -> answer.isNotEmpty() }.distinct()

    /** Whether the server would take [draft]: a title, 1 to 10 answers, codes of one length. */
    fun isValid(draft: MissionDraft): Boolean {
        val answers = cleanAnswers(draft.answers)
        if (!isValidTitle(draft.title) || draft.statement.trim().length > STATEMENT_MAX_LENGTH) return false
        if (answers.isEmpty() || answers.size > ANSWERS_MAX) return false
        return when (draft.type) {
            MissionType.CODE -> answers.all(::isValidCode) && answers.map { answer -> answer.length }.distinct().size == 1
            MissionType.TEXT -> answers.all(::isValidTextAnswer)
        }
    }
}
