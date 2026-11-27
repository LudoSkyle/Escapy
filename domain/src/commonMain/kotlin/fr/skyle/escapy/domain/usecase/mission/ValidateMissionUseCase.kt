package fr.skyle.escapy.domain.usecase.mission

/** Validates a mission in the place of the players, in a started game the user runs: as if solved. */
interface ValidateMissionUseCase {
    suspend operator fun invoke(gameId: String, missionId: String)
}
