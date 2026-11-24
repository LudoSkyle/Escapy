package fr.skyle.escapy.domain.usecase.session

import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.data.repository.session.api.SessionRepository
import fr.skyle.escapy.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WatchSessionUseCaseImplTest {

    @Test
    fun invoke_mapsEachStoredSession_anonymousAsGuest() = runTest {
        // Given
        val repository = FakeSessionRepository(
            StoredSession.SignedOut,
            StoredSession.SignedIn(userId = "guest", isAnonymous = true),
            StoredSession.SignedIn(userId = "member", isAnonymous = false),
        )
        val useCase = WatchSessionUseCaseImpl(sessionRepository = repository)

        // When
        val sessions = useCase().toList()

        // Then
        assertEquals(
            listOf(
                Session.SignedOut,
                Session.SignedIn(userId = "guest", isGuest = true),
                Session.SignedIn(userId = "member", isGuest = false),
            ),
            sessions,
        )
    }

    private class FakeSessionRepository(
        private vararg val sessions: StoredSession,
    ) : SessionRepository {
        override fun watchSession(): Flow<StoredSession> = flowOf(*sessions)
    }
}
