package fr.skyle.escapy.domain.validation

/**
 * What the QR code of a game holds, and how a scanned one is read: the code of the game for now. A
 * link that opens the app (lot 6c) will replace it here, and links are already read.
 */
object JoinCodeQr {

    /** The content of the QR code of the game of [joinCode]. */
    fun contentOf(joinCode: String): String = joinCode

    /**
     * The code a scanned QR code holds: a code alone, or an http(s) link whose path ends with a code.
     * Null for anything else, which is not the QR code of a game and is never sent to the server.
     */
    fun joinCodeOf(content: String): String? {
        val trimmed = content.trim()
        val scheme = SCHEMES.firstOrNull { scheme -> trimmed.startsWith(scheme, ignoreCase = true) }
        val candidate = if (scheme == null) {
            trimmed
        } else {
            val segments = trimmed.substring(scheme.length)
                .substringBefore('?')
                .substringBefore('#')
                .split('/')
                .filter { segment -> segment.isNotEmpty() }
            // The host, then at least one segment: the code is the last one
            if (segments.size < 2) return null
            segments.last()
        }
        return GameRules.cleanJoinCode(candidate).takeIf(GameRules::isJoinCode)
    }

    private val SCHEMES = listOf("https://", "http://")
}
