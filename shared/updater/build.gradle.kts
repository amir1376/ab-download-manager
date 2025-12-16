import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Android.library)
    id(Plugins.Kotlin.serialization)
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
            api(libs.okhttp.okhttp)
            api(libs.kotlin.coroutines.core)
            implementation(project(":shared:utils"))
            implementation(libs.semver)
            implementation("ir.amirab.util:platform:1")
        }
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(libs.jna.platform)
        }
    }
}
android {
    namespace = "com.abdownloadmanager.updater"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
}
