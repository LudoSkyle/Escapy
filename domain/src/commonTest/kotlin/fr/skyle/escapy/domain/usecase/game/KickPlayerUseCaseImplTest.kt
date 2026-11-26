package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KickPlayerUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    @Test
    fun `excluding a player reaches the server`() = runTest {
        // When
        KickPlayerUseCaseImpl(gameRepository = gameRepository)(gameId = "game", playerId = "player")

        // Then
        assertEquals(expected = listOf("kick:game:player"), actual = gameRepository.calls)
    }
}
