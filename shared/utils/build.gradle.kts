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
        commonMain.dependencies {
            implementation(libs.kotlin.serialization.json)
            api(libs.okio.okio)
            api(libs.okhttp.okhttp)
            api(libs.kotlin.coroutines.core)
            api(libs.kotlin.datetime)
            api(libs.semver)
            api(libs.arrow.optics)
            api("ir.amirab.util:platform:1")
        }
        val desktopMain by getting
        desktopMain.dependencies {
            api(libs.jna.platform)
        }
        androidMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.androidx.core.ktx)
        }
    }
}
android {
    compileSdk = 36
    namespace = "ir.amirab.util"
    defaultConfig {
        minSdk = 26
    }
}
