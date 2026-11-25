plugins {
    id("escapy-compose-library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    android {
        namespace = "fr.skyle.escapy.shared"

        withHostTest {
            // Kermit logs with android.util.Log, which the JVM of the host tests does not implement:
            // a test that goes through a log would fail on that call alone
            isReturnDefaultValues = true
            // Robolectric draws the screens on the JVM, with the strings and drawables of the module
            isIncludeAndroidResources = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            // Static: the Firebase iOS SDK is linked once, by the app, through Swift Package Manager
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Modules
            implementation(project(":designSystem"))
            implementation(project(":domain"))

            // Compose
            implementation(libs.composeMultiplatform.ui)
            implementation(libs.composeMultiplatform.foundation)
            implementation(libs.composeMultiplatform.resources)
            implementation(libs.composeMultiplatform.ui.tooling.preview)

            // Navigation and lifecycle
            implementation(libs.navigation3Multiplatform.ui)
            implementation(libs.navigationEventMultiplatform.compose)
            implementation(libs.lifecycleMultiplatform.runtime.compose)
            implementation(libs.lifecycleMultiplatform.viewmodel)
            implementation(libs.lifecycleMultiplatform.viewmodel.navigation3)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)

            // Logs
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
            implementation(libs.test.kotlinx.coroutines)
            implementation(libs.test.composeMultiplatform.ui)
        }

        iosMain.dependencies {
            // Firebase is started by the app of iosApp, through startFirebase()
            implementation(libs.gitlive.firebase.app)
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.robolectric)
            implementation(libs.test.androidx.compose.ui.manifest)
        }
    }
}

compose.resources {
    packageOfResClass = "fr.skyle.escapy.resources"
}
