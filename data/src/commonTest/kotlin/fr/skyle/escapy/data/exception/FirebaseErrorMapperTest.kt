package fr.skyle.escapy.data.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FirebaseErrorMapperTest {

    @Test
    fun `a wrong password an unknown email and invalid credentials are the same failure`() {
        // Given
        val codes = listOf("ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND", "ERROR_INVALID_CREDENTIAL", "ERROR_USER_DISABLED")

        // When
        val failures = codes.map(::authFailureOf).toSet()

        // Then
        assertEquals(
            expected = setOf(AuthFailure.INVALID_CREDENTIALS),
            actual = failures,
        )
    }

    @Test
    fun `each other code of Firebase Auth has its failure`() {
        assertEquals(expected = AuthFailure.EMAIL_ALREADY_IN_USE, actual = authFailureOf("ERROR_EMAIL_ALREADY_IN_USE"))
        assertEquals(expected = AuthFailure.EMAIL_ALREADY_IN_USE, actual = authFailureOf("ERROR_CREDENTIAL_ALREADY_IN_USE"))
        assertEquals(expected = AuthFailure.WEAK_PASSWORD, actual = authFailureOf("ERROR_WEAK_PASSWORD"))
        assertEquals(expected = AuthFailure.INVALID_EMAIL, actual = authFailureOf("ERROR_INVALID_EMAIL"))
        assertEquals(expected = AuthFailure.TOO_MANY_REQUESTS, actual = authFailureOf("ERROR_TOO_MANY_REQUESTS"))
        assertEquals(expected = AuthFailure.RECENT_SIGN_IN_REQUIRED, actual = authFailureOf("ERROR_REQUIRES_RECENT_LOGIN"))
        assertEquals(expected = AuthFailure.UNKNOWN, actual = authFailureOf("ERROR_SOMETHING_NEW"))
        assertEquals(expected = AuthFailure.UNKNOWN, actual = authFailureOf(null))
    }

    @Test
    fun `the reason of a refused call is read from its details`() {
        assertEquals(
            expected = "AVATAR_INVALID",
            actual = functionReasonOf(mapOf("reason" to "AVATAR_INVALID")),
        )
        assertNull(functionReasonOf(null))
        assertNull(functionReasonOf("AVATAR_INVALID"))
        assertNull(functionReasonOf(mapOf("other" to "value")))
    }
}
