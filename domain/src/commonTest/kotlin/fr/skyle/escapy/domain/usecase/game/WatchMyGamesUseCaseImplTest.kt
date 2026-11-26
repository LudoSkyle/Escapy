package fr.skyle.escapy.domain.usecase.game

import fr.skyle.escapy.data.exception.NetworkUnavailableException as DataNetworkUnavailableException
import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.domain.exception.NetworkUnavailableException
import fr.skyle.escapy.domain.fake.FakeGameRepository
import fr.skyle.escapy.domain.fake.FakeSessionRepository
import fr.skyle.escapy.domain.fake.storedGameEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WatchMyGamesUseCaseImplTest {

    private val sessionRepository = FakeSessionRepository()
    private val gameRepository = FakeGameRepository()

    private val watchMyGamesUseCase = WatchMyGamesUseCaseImpl(
        sessionRepository = sessionRepository,
        gameRepository = gameRepository,
    )

    private fun signedIn(userId: String) =
        StoredSession.SignedIn(userId = userId, isAnonymous = true, email = null, hasPassword = false)

    @Test
    fun `the games of the signed-in user tell which ones they run`() = runTest {
        // Given
        sessionRepository.sessions = flowOf(signedIn(userId = "master"))
        gameRepository.gamesOf = flowOf(listOf(storedGameEntry(gameId = "own", isMaster = true), storedGameEntry(gameId = "other", isMaster = false)))

        // When
        val games = watchMyGamesUseCase().first()

        // Then
        assertEquals(expected = listOf("watchGamesOf:master"), actual = gameRepository.calls)
        assertTrue(games[0].isMaster)
        assertFalse(games[1].isMaster)
    }

    @Test
    fun `a signed-out user has no game`() = runTest {
        // Given
        sessionRepository.sessions = flowOf(StoredSession.SignedOut)

        // When
        val games = watchMyGamesUseCase().toList()

        // Then
        assertEquals(expected = listOf(emptyList()), actual = games)
    }

    @Test
    fun `a failure to read is told in the terms of the domain`() = runTest {
        // Given
        sessionRepository.sessions = flowOf(signedIn(userId = "master"))
        gameRepository.gamesOf = flow { throw DataNetworkUnavailableException() }

        // When / Then
        assertFailsWith<NetworkUnavailableException> {
            watchMyGamesUseCase().first()
        }
    }
}
