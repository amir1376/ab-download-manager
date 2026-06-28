import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Android.kotlinMultiplatformLibrary)
    id(Plugins.Kotlin.serialization)
}
kotlin {
    jvm("desktop")
    android {
        namespace = "com.abdownloadmanager.updater"
        compileSdk = 36
        minSdk = 26
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.serialization.json)
            api(libs.okhttp.okhttp)
            api(libs.kotlin.coroutines.core)
            implementation(project(":shared:utils"))
            implementation(libs.semver)
            implementation("com.xeton.util:platform:1")
        }
        val desktopMain = sourceSets.getByName("desktopMain")
        desktopMain.dependencies {
            implementation(libs.jna.platform)
        }
    }
}
