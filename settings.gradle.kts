import java.net.URI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.walt.id/repository/waltid/")
        maven("https://maven.walt.id/repository/waltid-ssi-kit/")
        maven("https://repo.danubetech.com/repository/maven-public/")
    }
}




rootProject.name = "vc-wallet-android-app"
include(":app")
include(":verifiable-credentials-library")
