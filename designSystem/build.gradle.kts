plugins {
    id("escapy-compose-library")
}

kotlin {
    android {
        namespace = "fr.skyle.escapy.designsystem"

        withHostTest {
            // Robolectric draws the components on the JVM, with the fonts and strings of the module
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(libs.composeMultiplatform.ui)
            implementation(libs.composeMultiplatform.foundation)
            implementation(libs.composeMultiplatform.material3)
            implementation(libs.composeMultiplatform.resources)
            implementation(libs.composeMultiplatform.ui.tooling.preview)
        }

        commonTest.dependencies {
            implementation(libs.test.kotlin)
            implementation(libs.test.composeMultiplatform.ui)
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.test.junit)
            implementation(libs.test.robolectric)
            implementation(libs.test.androidx.compose.ui.manifest)
        }
    }
}

compose.resources {
    packageOfResClass = "fr.skyle.escapy.designsystem.resources"
}
