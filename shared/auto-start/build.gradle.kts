import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
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
            implementation(project(":shared:utils"))
        }
        val desktopMain by getting
        desktopMain.dependencies {
            //    // for windows, we use registry
            implementation(libs.jna.platform)
        }
    }
}

android {
    compileSdk = 36
    namespace = "ir.amirab.util.startup"
    defaultConfig {
        minSdk = 26
    }
}
