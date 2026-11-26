package fr.skyle.escapy

import androidx.compose.ui.window.ComposeUIViewController
import fr.skyle.escapy.platform.camera.SimulatedQrCode
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun MainViewController(): UIViewController {
    // A debug build launched with ESCAPY_SIMULATED_QR=<content> reads it as if the camera saw it:
    // the simulator has no camera
    if (Platform.isDebugBinary) {
        SimulatedQrCode.content = NSProcessInfo.processInfo.environment["ESCAPY_SIMULATED_QR"] as? String
    }
    return ComposeUIViewController {
        // A debug build launched with ESCAPY_CATALOG=1 shows the design system instead of the app
        if (Platform.isDebugBinary && NSProcessInfo.processInfo.environment["ESCAPY_CATALOG"] == "1") {
            EscapyDesignSystemCatalog()
        } else {
            EscapyApp()
        }
    }
}
