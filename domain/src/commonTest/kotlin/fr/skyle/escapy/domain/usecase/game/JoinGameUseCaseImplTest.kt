package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.ExcludedFromGameException
import fr.skyle.escapy.domain.exception.GameFullException
import fr.skyle.escapy.domain.exception.GameNotFoundException
import fr.skyle.escapy.domain.exception.TooManyAttemptsException
import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JoinGameUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    @Test
    fun `a code is sent without its spaces in capitals`() = runTest {
        // When
        val gameId = JoinGameUseCaseImpl(gameRepository = gameRepository)(code = " abc 234 ")

        // Then
        assertEquals(expected = "game", actual = gameId)
        assertEquals(expected = listOf("join:ABC234"), actual = gameRepository.calls)
    }

    @Test
    fun `each refusal to join has its exception`() = runTest {
        val joinGameUseCase = JoinGameUseCaseImpl(gameRepository = gameRepository)

        gameRepository.failure = FunctionFailureException(reason = "GAME_NOT_FOUND")
        assertFailsWith<GameNotFoundException> { joinGameUseCase(code = "ABC234") }

        gameRepository.failure = FunctionFailureException(reason = "GAME_FULL")
        assertFailsWith<GameFullException> { joinGameUseCase(code = "ABC234") }

        gameRepository.failure = FunctionFailureException(reason = "PLAYER_EXCLUDED")
        assertFailsWith<ExcludedFromGameException> { joinGameUseCase(code = "ABC234") }

        gameRepository.failure = FunctionFailureException(reason = "TOO_MANY_ATTEMPTS")
        assertFailsWith<TooManyAttemptsException> { joinGameUseCase(code = "ABC234") }
    }
}
