package fr.skyle.escapy.domain.usecase.mission

import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.GamePausedException
import fr.skyle.escapy.domain.exception.TooManyAnswersException
import fr.skyle.escapy.domain.fake.FakeMissionRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SubmitAnswerUseCaseImplTest {

    private val missionRepository = FakeMissionRepository()

    private val submitAnswerUseCase = SubmitAnswerUseCaseImpl(missionRepository = missionRepository)

    @Test
    fun `an answer is sent trimmed and the server says whether it is right`() = runTest {
        // Given
        missionRepository.isCorrect = true

        // When
        val isCorrect = submitAnswerUseCase(gameId = "game", missionId = "safe", answer = " 0427 ")

        // Then
        assertTrue(isCorrect)
        assertEquals(expected = listOf("submit:safe:0427"), actual = missionRepository.calls)
    }

    @Test
    fun `a paused game and too many answers are told`() = runTest {
        // When Then
        missionRepository.failure = FunctionFailureException(reason = "GAME_PAUSED")
        assertFailsWith<GamePausedException> { submitAnswerUseCase(gameId = "game", missionId = "safe", answer = "0427") }
        missionRepository.failure = FunctionFailureException(reason = "TOO_MANY_ANSWERS")
        assertFailsWith<TooManyAnswersException> { submitAnswerUseCase(gameId = "game", missionId = "safe", answer = "0427") }
    }
}
