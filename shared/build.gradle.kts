plugins {
    id("escapy-compose-library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// The namespace of the Kotlin bindings of the SwiftPM packages: swiftPMImport.fr.skyle.escapy.shared
group = "fr.skyle.escapy"

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

    swiftPMDependencies {
        // Only the module the code calls: the modules GoogleSignIn depends on need no Kotlin bindings
        discoverClangModulesImplicitly.set(false)
        // The native sign-in of Google, which gives the ID token Firebase signs in with
        swiftPackage(
            url = url("https://github.com/google/GoogleSignIn-iOS.git"),
            version = from(libs.versions.googleSignInIos.get()),
            products = listOf(product("GoogleSignIn")),
            importedClangModules = listOf("GoogleSignIn"),
        )
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

        androidMain.dependencies {
            // Google sign-in
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.playServicesAuth)
            implementation(libs.googleId)
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
