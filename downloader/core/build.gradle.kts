import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Kotlin.serialization)
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
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlin.serialization.json)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.coroutines.core)
                api(libs.okio.okio)
                api(libs.okhttp.okhttp)
                api(libs.okhttp.coroutines)
                implementation(project(":shared:utils"))
                api("io.lindstrom:m3u8-parser:0.29")
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
