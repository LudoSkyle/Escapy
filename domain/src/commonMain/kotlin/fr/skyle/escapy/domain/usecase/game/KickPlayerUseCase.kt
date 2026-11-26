package fr.skyle.escapy.domain.usecase.game

/** Excludes a player from a game the user runs: the code no longer lets them in. */
interface KickPlayerUseCase {
    suspend operator fun invoke(gameId: String, playerId: String)
}
