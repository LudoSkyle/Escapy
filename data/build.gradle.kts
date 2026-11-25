plugins {
    id("escapy-kmp-library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    android {
        namespace = "fr.skyle.escapy.data"

        withHostTest {
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)

            // Koin
            implementation(libs.koin.core)

            // Firebase: the iOS SDK comes through Swift Package Manager, see iosApp/CLAUDE.md
            implementation(libs.gitlive.firebase.app)
            implementation(libs.gitlive.firebase.auth)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.functions)
            implementation(libs.gitlive.firebase.storage)
        }

        androidMain.dependencies {
            // Koin
            implementation(libs.koin.android)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
            implementation(libs.test.kotlinx.coroutines)
        }
    }
}
