package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CancelGameUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    @Test
    fun `cancelling reaches the server`() = runTest {
        // When
        CancelGameUseCaseImpl(gameRepository = gameRepository)(gameId = "game")

        // Then
        assertEquals(expected = listOf("cancel:game"), actual = gameRepository.calls)
    }
}
