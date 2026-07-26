plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Kotlin.serialization)
    id(Plugins.Android.multiplatformLibrary)
}
kotlin {
    jvm("desktop")
    android {
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        namespace = "ir.amirab.util"
        minSdk = 26
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
            api(libs.kermit)
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
