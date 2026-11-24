package fr.skyle.escapy

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun MainViewController(): UIViewController =
    ComposeUIViewController {
        // A debug build launched with ESCAPY_CATALOG=1 shows the design system instead of the app
        if (Platform.isDebugBinary && NSProcessInfo.processInfo.environment["ESCAPY_CATALOG"] == "1") {
            EscapyDesignSystemCatalog()
        } else {
            EscapyApp()
        }
    }
