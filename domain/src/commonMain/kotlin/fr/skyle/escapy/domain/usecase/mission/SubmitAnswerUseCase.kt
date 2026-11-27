package fr.skyle.escapy.domain.usecase.mission

/**
 * Sends an answer to an active mission, and returns whether the server found it right: the app never
 * decides. Throws GamePausedException, GameFinishedException, MissionNotFoundException,
 * TooManyAnswersException, NetworkUnavailableException.
 */
interface SubmitAnswerUseCase {
    suspend operator fun invoke(gameId: String, missionId: String, answer: String): Boolean
}
