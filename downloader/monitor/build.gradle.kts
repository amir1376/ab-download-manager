import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Kotlin.serialization)
    id(MyPlugins.composeBase)
    id(Plugins.Android.kotlinMultiplatformLibrary)
}
kotlin {
    jvm("desktop")
    android {
        compileSdk = 36
        namespace = "ir.amirab.downloader.monitor"
        minSdk = 26
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":downloader:core"))
                implementation(project(":shared:utils"))
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.compose.runtime)
            }
        }
    }
}
