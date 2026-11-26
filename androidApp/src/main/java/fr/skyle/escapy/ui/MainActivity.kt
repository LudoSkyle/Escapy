package fr.skyle.escapy.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import fr.skyle.escapy.BuildConfig
import fr.skyle.escapy.EscapyApp
import fr.skyle.escapy.EscapyDesignSystemCatalog
import fr.skyle.escapy.platform.camera.SimulatedQrCode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // The icon of the app while it starts, on every version of Android
        installSplashScreen()
        // Light icons on the system bars: the app is dark only
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        // A debug build started with --ez catalog true shows the design system instead of the app
        val showsCatalog = BuildConfig.DEBUG && intent.getBooleanExtra(EXTRA_CATALOG, false)
        // A debug build started with --es simulatedQr <content> reads it as if the camera saw it
        if (BuildConfig.DEBUG) {
            SimulatedQrCode.content = intent.getStringExtra(EXTRA_SIMULATED_QR)
        }

        setContent {
            if (showsCatalog) {
                EscapyDesignSystemCatalog()
            } else {
                EscapyApp()
            }
        }
    }

    private companion object {
        const val EXTRA_CATALOG = "catalog"
        const val EXTRA_SIMULATED_QR = "simulatedQr"
    }
}
