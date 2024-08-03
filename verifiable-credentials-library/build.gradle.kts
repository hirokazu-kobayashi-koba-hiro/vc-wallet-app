plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.diffplug.spotless")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "1.8.22"
}

android {
    namespace = "org.idp.wallet.verifiable_credentials_library"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["vcWalletDomain"] = "@string/com_vc_wallet_domain"
        manifestPlaceholders["vcWalletScheme"] = "@string/com_vc_wallet_scheme"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.activity:activity-ktx:1.9.0")

    //jwt
    implementation("com.nimbusds:nimbus-jose-jwt:9.40")
    //https://github.com/eu-digital-identity-wallet/eudi-lib-jvm-sdjwt-kt
    implementation("eu.europa.ec.eudi:eudi-lib-jvm-sdjwt-kt:0.5.0")
    implementation("id.walt:waltid-sd-jwt-jvm:1.2306191408.0")

    //blockchain
    ////web3
    implementation("org.web3j:core:4.10.2")
    implementation("org.web3j:crypto:5.0.0")

    ////MerkleTree
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.16.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    ////Base58
    implementation("com.github.multiformats:java-multibase:v1.1.1")
    //cbor
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.6.3")

    //compose
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material:1.6.8")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite-android:1.3.0-beta05")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.35.1-alpha")

    //state
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    //JSON
    implementation("com.beust:klaxon:5.6")
    //JSONPath refer to https://identity.foundation/presentation-exchange/spec/v2.0.0/#jsonpath-implementations
    implementation("com.nfeld.jsonpathkt:jsonpathkt:2.0.1")
    //QR
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.1.0")

    //custom tabs
    implementation("androidx.browser:browser:1.8.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.1")

    //sql
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:2.6.1")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

spotless { // if you are using build.gradle.kts, instead of 'spotless {' use:
    // configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        target("/src/**/*.kt")
        ktfmt()
    }
}