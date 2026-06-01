import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val keystorePropertiesFile = rootProject.file("local.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/veritas-reader-test-release.jks")
            storePassword = keystoreProperties.getProperty("RELEASE_STORE_PASSWORD") ?: "1st2usjan"
            keyAlias = "veritasreader"
            keyPassword = keystoreProperties.getProperty("RELEASE_KEY_PASSWORD") ?: "1st2usjan"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    namespace = "com.veritas.reader"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.veritas.reader"
        minSdk = 28
        targetSdk = 36
        versionCode = 24
        versionName = "0.3.7-beta"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.05.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.media3:media3-session:1.10.1")
    implementation("androidx.pdf:pdf-viewer-fragment:1.0.0-alpha18")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    constraints {
        implementation("org.bouncycastle:bcpkix-jdk15to18:1.84") {
            because("Keep PDFBox Android's certificate/parser dependency line current for lint and security review.")
        }
        implementation("org.bouncycastle:bcprov-jdk15to18:1.84") {
            because("Keep PDFBox Android's crypto provider dependency line current for lint and security review.")
        }
        implementation("org.bouncycastle:bcutil-jdk15to18:1.84") {
            because("Keep PDFBox Android's Bouncy Castle utility dependency line current for lint and security review.")
        }
    }
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
