package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.exception.FunctionFailureException
import fr.skyle.escapy.domain.exception.InvalidGameTitleException
import fr.skyle.escapy.domain.exception.TooManyGamesException
import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateGameUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    @Test
    fun `a created game returns its id`() = runTest {
        // When
        val gameId = CreateGameUseCaseImpl(gameRepository = gameRepository)(
            title = "Le manoir",
            durationMinutes = 90,
            maxPlayers = null,
        )

        // Then
        assertEquals(expected = "game", actual = gameId)
        assertEquals(expected = listOf("create:Le manoir:90:null"), actual = gameRepository.calls)
    }

    @Test
    fun `a title or a sixth game refused by the server is told`() = runTest {
        val createGameUseCase = CreateGameUseCaseImpl(gameRepository = gameRepository)

        gameRepository.failure = FunctionFailureException(reason = "GAME_TITLE_INVALID")
        assertFailsWith<InvalidGameTitleException> { createGameUseCase(title = "Ab", durationMinutes = 60, maxPlayers = null) }

        gameRepository.failure = FunctionFailureException(reason = "TOO_MANY_GAMES")
        assertFailsWith<TooManyGamesException> { createGameUseCase(title = "Le manoir", durationMinutes = 60, maxPlayers = 4) }
    }
}
