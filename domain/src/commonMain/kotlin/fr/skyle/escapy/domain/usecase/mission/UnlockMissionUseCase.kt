package fr.skyle.escapy.domain.usecase.mission

/** Unlocks a mission by hand, in a started game the user runs, whatever its trigger. */
interface UnlockMissionUseCase {
    suspend operator fun invoke(gameId: String, missionId: String)
}
