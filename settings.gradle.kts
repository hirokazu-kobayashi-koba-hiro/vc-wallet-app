import java.net.URI

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "vc-wallet-android-app"
include(":app")
include(":verifiable-credentials-library")
