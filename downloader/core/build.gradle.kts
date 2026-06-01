import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Kotlin.serialization)
    id(Plugins.Android.library)
}
kotlin {
    jvm("desktop")
    androidTarget("android") {
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlin.serialization.json)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.coroutines.core)
                implementation(project(":shared:utils"))
                // UniFFI generated bindings for xeton_core are located in src/commonMain/kotlin/ir/amirab/xeton_core_ffi
                // and will be automatically picked up by the commonMain source set.
            }
        }
    }
}
android {
    compileSdk = 36
    namespace = "ir.amirab.downloader.core"
    defaultConfig {
        minSdk = 26
    }
}
