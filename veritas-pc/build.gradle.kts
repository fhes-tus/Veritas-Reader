plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
                implementation("org.json:json:20240303")
                implementation("org.apache.pdfbox:pdfbox:2.0.30")
                implementation("net.java.dev.jna:jna:5.14.0")
                implementation("net.java.dev.jna:jna-platform:5.14.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.veritas.desktop.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "VeritasReader"
            packageVersion = "2.0.0"
        }
    }
}

tasks.register("prepareKotlinBuildScriptModel") {
    group = "ide"
    description = "Stub task to prevent Android Studio import failures in non-android modules"
}
