import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// The version of the app, changed by a pull request; iOS follows it (iosApp/project.yml)
val APP_VERSION_NAME = "2.0"

// One version code per version name, the same on every build: 2.0 gives 20000, 2.1.3 gives 20103
fun String.toVersionCode(): Int {
    val (major, minor, patch) = (split(".").map(String::toInt) + listOf(0, 0)).take(3)
    return major * 10_000 + minor * 100 + patch
}

// The private submodule holds the signing keys and the Firebase files. Without it, a fork or the CI
// still builds and tests: the debug build is signed with the default key of Android, the release is
// not signed, and Firebase is left out. The API key of the Firebase project only accepts the apps
// signed with the keys of the submodule
val keystorePropertiesFile = rootProject.file("private/keys/keystore.properties")
val hasKeystore = keystorePropertiesFile.exists()
val keystoreProperties = Properties()
if (hasKeystore) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}
val hasFirebase = rootProject.file("private/firebase/google-services.json").exists()
if (hasFirebase) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "fr.skyle.escapy"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "fr.skyle.escapy"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionName = APP_VERSION_NAME
        versionCode = APP_VERSION_NAME.toVersionCode()
    }

    signingConfigs {
        if (hasKeystore) {
            getByName("debug") {
                storeFile = file(keystoreProperties["debugStoreFile"].toString())
            }
            create("release") {
                storeFile = file(keystoreProperties["releaseStoreFile"].toString())
                storePassword = keystoreProperties["passwordRelease"].toString()
                keyAlias = keystoreProperties["aliasRelease"].toString()
                keyPassword = keystoreProperties["passwordRelease"].toString()
            }
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
            applicationIdSuffix = ".debug"
            // The Firebase emulators of the Mac, on demand: -Pescapy.firebaseEmulator=true
            buildConfigField(
                "boolean",
                "USE_FIREBASE_EMULATOR",
                (findProperty("escapy.firebaseEmulator") == "true").toString(),
            )
        }
        release {
            buildConfigField("boolean", "USE_FIREBASE_EMULATOR", "false")
            isMinifyEnabled = true
            isShrinkResources = true

            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

tasks.register("clonePrivateFiles") {
    notCompatibleWithConfigurationCache("custom")
    doLast {
        copy {
            from("../private/firebase/google-services.json")
            into("./")
        }
    }
}

project.afterEvaluate {
    getTasks().getByName("preBuild").finalizedBy("clonePrivateFiles")
}

dependencies {
    // Modules
    implementation(project(":shared"))

    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    // Koin
    implementation(libs.koin.android)

    // Logs
    implementation(libs.kermit)
}
