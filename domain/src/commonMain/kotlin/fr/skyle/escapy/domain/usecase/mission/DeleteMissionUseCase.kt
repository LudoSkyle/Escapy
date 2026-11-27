package fr.skyle.escapy.domain.usecase.mission

/** Deletes a mission of a game the user runs; the missions after it are unlocked by hand from then on. */
interface DeleteMissionUseCase {
    suspend operator fun invoke(gameId: String, missionId: String)
}
