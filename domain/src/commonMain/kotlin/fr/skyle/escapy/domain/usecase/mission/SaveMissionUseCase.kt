package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.domain.model.MissionDraft

/**
 * Creates or changes a mission of a game the user runs, and returns its id. A new mission gets its id
 * here, so that a creation sent again creates nothing twice. Throws InvalidMissionException,
 * TooManyMissionsException, GameFinishedException.
 */
interface SaveMissionUseCase {
    suspend operator fun invoke(gameId: String, draft: MissionDraft): String
}
