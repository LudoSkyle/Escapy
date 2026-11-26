package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LeaveGameUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    @Test
    fun `leaving reaches the server`() = runTest {
        // When
        LeaveGameUseCaseImpl(gameRepository = gameRepository)(gameId = "game")

        // Then
        assertEquals(expected = listOf("leave:game"), actual = gameRepository.calls)
    }
}
