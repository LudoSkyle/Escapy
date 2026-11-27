package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StartGameUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    @Test
    fun `starting reaches the server`() = runTest {
        // When
        StartGameUseCaseImpl(gameRepository = gameRepository)(gameId = "game")

        // Then
        assertEquals(expected = listOf("start:game"), actual = gameRepository.calls)
    }
}
