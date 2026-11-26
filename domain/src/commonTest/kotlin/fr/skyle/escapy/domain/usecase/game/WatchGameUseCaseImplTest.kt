package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.exception.AccessDeniedException
import fr.skyle.escapy.data.model.StoredMember
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.domain.fake.FakeGameRepository
import fr.skyle.escapy.domain.fake.FakeSessionRepository
import fr.skyle.escapy.domain.fake.storedGame
import fr.skyle.escapy.domain.model.GameMember
import fr.skyle.escapy.domain.model.GameState
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WatchGameUseCaseImplTest {

    private val sessionRepository = FakeSessionRepository().apply {
        sessions = flowOf(StoredSession.SignedIn(userId = "player", isAnonymous = true, email = null, hasPassword = false))
    }
    private val gameRepository = FakeGameRepository()

    private val watchGameUseCase = WatchGameUseCaseImpl(
        sessionRepository = sessionRepository,
        gameRepository = gameRepository,
    )

    private val player = StoredMember(userId = "player", username = "Joueuse", avatarId = 8)

    @Test
    fun `a game comes with its players the user among them`() = runTest {
        // Given
        gameRepository.game = flowOf(storedGame(masterId = "master"))
        gameRepository.players = flowOf(listOf(player))

        // When
        val states = watchGameUseCase(gameId = "game").toList()

        // Then
        val state = assertIs<GameState.Available>(states.single())
        assertEquals(expected = false, actual = state.game.isMaster)
        assertEquals(
            expected = listOf(GameMember(userId = "player", username = "Joueuse", avatarId = 8, isCurrentUser = true)),
            actual = state.players,
        )
    }

    @Test
    fun `a deleted game is the last state whatever the players do`() = runTest {
        // Given
        gameRepository.game = flowOf(storedGame(), null, storedGame())
        gameRepository.players = flow {
            emit(listOf(player))
            throw AccessDeniedException()
        }

        // When
        val states = watchGameUseCase(gameId = "game").toList()

        // Then
        assertIs<GameState.Available>(states.first())
        assertEquals(expected = GameState.Deleted, actual = states.last())
        assertEquals(expected = 1, actual = states.count { state -> state == GameState.Deleted })
    }

    @Test
    fun `a game that can no longer be read tells the user they were excluded`() = runTest {
        // Given
        gameRepository.game = flow {
            emit(storedGame())
            throw AccessDeniedException()
        }
        gameRepository.players = flowOf(listOf(player))

        // When
        val states = watchGameUseCase(gameId = "game").toList()

        // Then
        assertTrue(states.first() is GameState.Available)
        assertEquals(expected = GameState.Excluded, actual = states.last())
    }
}
