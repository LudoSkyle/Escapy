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
