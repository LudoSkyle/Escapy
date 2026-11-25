package fr.skyle.escapy.domain.usecase.session

import fr.skyle.escapy.data.model.StoredSession
import fr.skyle.escapy.domain.fake.FakeSessionRepository
import fr.skyle.escapy.domain.model.Session
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WatchSessionUseCaseImplTest {

    private val sessionRepository = FakeSessionRepository()

    private val watchSessionUseCase = WatchSessionUseCaseImpl(
        sessionRepository = sessionRepository,
    )

    @Test
    fun `an anonymous user is a guest`() = runTest {
        // Given
        sessionRepository.sessions = flowOf(
            StoredSession.SignedIn(
                userId = "guest",
                isAnonymous = true,
                email = null,
                hasPassword = false,
            ),
        )

        // When
        val session = watchSessionUseCase().first()

        // Then
        assertEquals(
            expected = Session.SignedIn(
                userId = "guest",
                isGuest = true,
            ),
            actual = session,
        )
    }

    @Test
    fun `a user with an account is a member`() = runTest {
        // Given
        sessionRepository.sessions = flowOf(
            StoredSession.SignedIn(
                userId = "member",
                isAnonymous = false,
                email = "member@escapy.app",
                hasPassword = true,
            ),
        )

        // When
        val session = watchSessionUseCase().first()

        // Then
        assertEquals(
            expected = Session.SignedIn(
                userId = "member",
                isGuest = false,
                email = "member@escapy.app",
                hasPassword = true,
            ),
            actual = session,
        )
    }

    @Test
    fun `a member who signs in with Google only has no password`() = runTest {
        // Given
        sessionRepository.sessions = flowOf(
            StoredSession.SignedIn(
                userId = "member",
                isAnonymous = false,
                email = "member@gmail.com",
                hasPassword = false,
            ),
        )

        // When
        val session = watchSessionUseCase().first()

        // Then
        assertEquals(
            expected = Session.SignedIn(
                userId = "member",
                isGuest = false,
                email = "member@gmail.com",
                hasPassword = false,
            ),
            actual = session,
        )
    }

    @Test
    fun `each change of session is followed`() = runTest {
        // Given
        sessionRepository.sessions = flowOf(
            StoredSession.SignedOut,
            StoredSession.SignedIn(
                userId = "guest",
                isAnonymous = true,
                email = null,
                hasPassword = false,
            ),
            StoredSession.SignedOut,
        )

        // When
        val sessions = watchSessionUseCase().toList()

        // Then
        assertEquals(
            expected = listOf(
                Session.SignedOut,
                Session.SignedIn(
                    userId = "guest",
                    isGuest = true,
                ),
                Session.SignedOut,
            ),
            actual = sessions,
        )
    }
}
