package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.GameNotStartedException
import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PauseGameUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    private val useCase = PauseGameUseCaseImpl(gameRepository = gameRepository)

    @Test
    fun `pausing reaches the server`() = runTest {
        // When
        useCase(gameId = "game")

        // Then
        assertEquals(expected = listOf("pause:game"), actual = gameRepository.calls)
    }

    @Test
    fun `a game not started yet is told`() = runTest {
        // Given
        gameRepository.failure = FunctionFailureException(reason = "GAME_NOT_STARTED")

        // When Then
        assertFailsWith<GameNotStartedException> { useCase(gameId = "game") }
    }
}
