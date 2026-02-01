import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Kotlin.serialization)
    id(MyPlugins.composeBase)
    id(Plugins.Android.library)
}
kotlin {
    jvm("desktop")
    androidTarget("android") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
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
android {
    compileSdk = 36
    namespace = "ir.amirab.downloader.monitor"
    defaultConfig {
        minSdk = 26
    }
}
