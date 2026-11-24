plugins {
    id("escapy-compose-library")
}

kotlin {
    android {
        namespace = "fr.skyle.escapy.designsystem"
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
    }
}

compose.resources {
    packageOfResClass = "fr.skyle.escapy.designsystem.resources"
}
