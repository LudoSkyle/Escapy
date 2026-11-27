package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.domain.fake.FakeGameRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ResumeGameUseCaseImplTest {

    private val gameRepository = FakeGameRepository()

    @Test
    fun `resuming reaches the server`() = runTest {
        // When
        ResumeGameUseCaseImpl(gameRepository = gameRepository)(gameId = "game")

        // Then
        assertEquals(expected = listOf("resume:game"), actual = gameRepository.calls)
    }
}
