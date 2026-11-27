package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpireGameUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    @Test
    fun `the time up reaches the server`() = runTest {
        // When
        ExpireGameUseCaseImpl(gameRepository = gameRepository)(gameId = "game")

        // Then
        assertEquals(expected = listOf("expire:game"), actual = gameRepository.calls)
    }
}
