package fr.skyle.escapy.domain.validation

/**
 * The rules of a chat message, the same as the Firestore rules check: a text with something else
 * than spaces, of 500 UTF-16 units at most, as String.length counts (an emoji counts two).
 */
object ChatRules {

    const val MESSAGE_MAX_LENGTH = 500

    /** The message as sent: without the spaces and line breaks around it. */
    fun cleanMessage(text: String): String = text.trim()

    fun isValidMessage(text: String): Boolean {
        val message = cleanMessage(text)
        return message.isNotEmpty() && message.length <= MESSAGE_MAX_LENGTH
    }
}
