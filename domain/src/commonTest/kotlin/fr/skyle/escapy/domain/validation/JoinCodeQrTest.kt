package fr.skyle.escapy.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JoinCodeQrTest {

    @Test
    fun `the QR code of a game holds its code and reads back`() {
        assertEquals(expected = "ABC234", actual = JoinCodeQr.joinCodeOf(JoinCodeQr.contentOf(joinCode = "ABC234")))
    }

    @Test
    fun `a code alone is read without its case and its spaces`() {
        assertEquals(expected = "ABC234", actual = JoinCodeQr.joinCodeOf(" abc 234\n"))
    }

    @Test
    fun `a link whose path ends with a code is read`() {
        assertEquals(expected = "ABC234", actual = JoinCodeQr.joinCodeOf("https://example.com/join/ABC234"))
        assertEquals(expected = "ABC234", actual = JoinCodeQr.joinCodeOf("HTTP://example.com/abc234/?from=qr#top"))
    }

    @Test
    fun `anything else is not the QR code of a game`() {
        assertNull(JoinCodeQr.joinCodeOf("https://example.com"))
        assertNull(JoinCodeQr.joinCodeOf("https://ABC234"))
        assertNull(JoinCodeQr.joinCodeOf("https://example.com/join/menu"))
        assertNull(JoinCodeQr.joinCodeOf("WIFI:S:Escapy;T:WPA;P:secret;;"))
        assertNull(JoinCodeQr.joinCodeOf("ABC23"))
        assertNull(JoinCodeQr.joinCodeOf("ABC2340"))
        assertNull(JoinCodeQr.joinCodeOf("ABCDE0"))
        assertNull(JoinCodeQr.joinCodeOf("geo:48.85,2.35"))
    }
}
