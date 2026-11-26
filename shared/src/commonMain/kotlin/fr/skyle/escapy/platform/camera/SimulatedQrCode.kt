package fr.skyle.escapy.platform.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.escapy.designsystem.core.text.ESText
import fr.skyle.escapy.designsystem.theme.ESTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Debug tool, never in a release build: a QR code content the camera reads as if it saw it, for the
 * screens after a scan to be tested where no camera can (the iOS simulator, an Android emulator). Set
 * by the host of a debug build (see docs/KMP/03_CODE_PLATEFORME.md); null otherwise.
 */
object SimulatedQrCode {
    var content: String? = null
}

/**
 * Stands for the camera while [SimulatedQrCode] holds a content: reads it a moment after reading
 * starts, then again each second, as a camera keeps seeing a code held in front of it. Its text is
 * not translated: only developers see it.
 */
@Composable
internal fun SimulatedQrCodeCamera(
    content: String,
    isReading: Boolean,
    onQrCodeRead: (content: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnQrCodeRead by rememberUpdatedState(onQrCodeRead)

    LaunchedEffect(isReading) {
        while (isReading) {
            delay(1.seconds)
            currentOnQrCodeRead(content)
        }
    }

    Box(modifier = modifier.background(color = Color.Black)) {
        ESText(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 96.dp, start = 32.dp, end = 32.dp),
            text = "Simulated camera: $content",
            style = ESTheme.typography.caption,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}
