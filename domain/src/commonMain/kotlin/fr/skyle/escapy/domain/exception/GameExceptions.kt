package fr.skyle.escapy.domain.exception

/** The title of a game is too short, too long, or has a control character. */
class InvalidGameTitleException(cause: Throwable? = null) : Exception("Invalid game title", cause)

/** The user already runs as many games as they may at once. */
class TooManyGamesException(cause: Throwable? = null) : Exception("Too many games", cause)

/** The code typed cannot be the code of a game: its length, or a character no code has. */
class InvalidJoinCodeException(cause: Throwable? = null) : Exception("Invalid join code", cause)

/** No game has this code, or the game was deleted. */
class GameNotFoundException(cause: Throwable? = null) : Exception("Game not found", cause)

/** The game no longer waits for players. */
class GameNotJoinableException(cause: Throwable? = null) : Exception("Game not joinable", cause)

/** The game has as many players as its game master allowed. */
class GameFullException(cause: Throwable? = null) : Exception("Game full", cause)

/** The game master excluded the user from this game. */
class ExcludedFromGameException(cause: Throwable? = null) : Exception("Excluded from the game", cause)

/** The action is for the game master of the game only. */
class NotGameMasterException(cause: Throwable? = null) : Exception("Not the game master", cause)

/** The timer of a game that has not started yet: the game master starts it first. */
class GameNotStartedException(cause: Throwable? = null) : Exception("Game not started", cause)

/** The game is over: its timer no longer changes. */
class GameFinishedException(cause: Throwable? = null) : Exception("Game finished", cause)

/** The minutes to add are out of bounds, or would add too much in all. */
class InvalidAddedTimeException(cause: Throwable? = null) : Exception("Invalid added time", cause)

/** The message is empty, or longer than ChatRules.MESSAGE_MAX_LENGTH. */
class InvalidMessageException(cause: Throwable? = null) : Exception("Invalid message", cause)

/** The chat refused the message: the game is over, or the user is no longer a member. */
class MessageRefusedException(cause: Throwable? = null) : Exception("Message refused", cause)
