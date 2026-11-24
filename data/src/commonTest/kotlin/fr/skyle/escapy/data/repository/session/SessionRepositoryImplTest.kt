package fr.skyle.escapy.data.repository.session

import fr.skyle.escapy.data.firebase.FirebaseStatus
import fr.skyle.escapy.data.model.StoredSession
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionRepositoryImplTest {

    @Test
    fun watchSession_withoutFirebase_isSignedOutOnly() = runTest {
        // Given
        val repository = SessionRepositoryImpl(firebaseStatus = FirebaseStatus { false })

        // When
        val sessions = repository.watchSession().toList()

        // Then
        assertEquals(listOf<StoredSession>(StoredSession.SignedOut), sessions)
    }
}
