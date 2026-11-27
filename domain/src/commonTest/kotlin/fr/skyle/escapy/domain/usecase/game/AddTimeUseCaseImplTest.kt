package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.GameFinishedException
import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AddTimeUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    private val useCase = AddTimeUseCaseImpl(gameRepository = gameRepository)

    @Test
    fun `adding time sends the minutes and those already added`() = runTest {
        // When
        useCase(gameId = "game", minutes = 5, addedMinutes = 10)

        // Then
        assertEquals(expected = listOf("addTime:game:5:10"), actual = gameRepository.calls)
    }

    @Test
    fun `a game over is told`() = runTest {
        // Given
        gameRepository.failure = FunctionFailureException(reason = "GAME_FINISHED")

        // When Then
        assertFailsWith<GameFinishedException> { useCase(gameId = "game", minutes = 5, addedMinutes = 10) }
    }
}
