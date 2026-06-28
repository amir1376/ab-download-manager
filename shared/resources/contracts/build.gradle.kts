import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Android.kotlinMultiplatformLibrary)
}
kotlin {
    jvm("desktop")
    android {
        compileSdk = 36
        namespace = "com.abdownloadmanager.resources.contracts"
        minSdk = 26
    }
    sourceSets.commonMain.dependencies {
        implementation(libs.okio.okio)
    }
}
