package fr.skyle.escapy.platform.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The preview of the back camera, filling its bounds, and the QR codes it reads while [isReading]:
 * each one is given as read, as often as the camera sees it. [onCameraUnavailable] tells a device with
 * no camera, as the iOS simulator. The permission is asked before, by the screen.
 */
@Composable
expect fun QrCodeCamera(
    isReading: Boolean,
    onQrCodeRead: (content: String) -> Unit,
    onCameraUnavailable: () -> Unit,
    modifier: Modifier = Modifier,
)
