import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Android.library)
}
kotlin {
    jvm("desktop")
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    sourceSets.commonMain.dependencies {
        implementation(libs.okio.okio)
    }
}
android {
    compileSdk = 36
    namespace = "com.abdownloadmanager.resources.contracts"
    defaultConfig {
        minSdk = 26
    }
}
