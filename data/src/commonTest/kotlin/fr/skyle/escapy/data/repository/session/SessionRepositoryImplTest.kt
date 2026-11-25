package fr.skyle.escapy.data.repository.session

import fr.skyle.escapy.data.firebase.FirebaseProvider
import fr.skyle.escapy.data.model.StoredSession
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionRepositoryImplTest {

    private val sessionRepository = SessionRepositoryImpl(
        firebaseProvider = FirebaseProvider { null },
    )

    @Test
    fun `without Firebase the session is signed out only`() = runTest {
        // Given
        // No Firebase file: a fork or the CI

        // When
        val sessions = sessionRepository.watchSession().toList()

        // Then
        assertEquals(
            expected = listOf<StoredSession>(StoredSession.SignedOut),
            actual = sessions,
        )
    }
}
