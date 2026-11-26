package fr.skyle.escapy.domain.model

/** What the user sees of a game they opened: the game with its players, or why it is gone. */
sealed interface GameState {

    data class Available(
        val game: Game,
        // The game master apart, in the order they joined
        val players: List<GameMember>,
    ) : GameState

    /** The game master cancelled the game, or it was deleted for nobody started it. */
    data object Deleted : GameState

    /** The user is no longer a member: the game master excluded them. */
    data object Excluded : GameState
}
