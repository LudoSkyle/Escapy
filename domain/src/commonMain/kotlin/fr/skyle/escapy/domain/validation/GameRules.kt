package fr.skyle.escapy.domain.validation

/**
 * The bounds of a game, which the server enforces (see docs/Backend/README.md): the screens offer
 * nothing else, so that a choice is never refused.
 */
object GameRules {

    const val TITLE_MIN_LENGTH = 3
    const val TITLE_MAX_LENGTH = 40

    const val DURATION_MIN_MINUTES = 15
    const val DURATION_MAX_MINUTES = 180
    const val DURATION_STEP_MINUTES = 5
    const val DURATION_DEFAULT_MINUTES = 60

    /** The most players a game takes, the game master apart, with no maximum set. */
    const val PLAYER_LIMIT = 20
    const val MAX_PLAYERS_MIN = 2

    const val JOIN_CODE_LENGTH = 6

    /** The code as the server reads it: without its spaces, in capitals. */
    fun cleanJoinCode(typed: String): String =
        typed.filterNot { character -> character.isWhitespace() }.uppercase()

    /** Whether a typed code has its length, the only thing worth checking before the server does. */
    fun isJoinCodeComplete(typed: String): Boolean =
        cleanJoinCode(typed).length == JOIN_CODE_LENGTH
}
