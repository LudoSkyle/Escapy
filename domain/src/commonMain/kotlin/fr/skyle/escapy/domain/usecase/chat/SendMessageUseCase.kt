package fr.skyle.escapy.domain.usecase.chat

/**
 * Sends a message in the chat of a game, in the name of the user, with the name and avatar of their
 * profile. Returns once the server acknowledged it; the message shows in the chat before. Throws
 * InvalidMessageException, MessageRefusedException (a game over, a user excluded),
 * NetworkUnavailableException.
 */
interface SendMessageUseCase {
    suspend operator fun invoke(gameId: String, text: String)
}
