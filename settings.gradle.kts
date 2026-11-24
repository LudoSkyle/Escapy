dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Escapy"
include(":androidApp")
include(":shared")
include(":designSystem")
include(":domain")
include(":data")
