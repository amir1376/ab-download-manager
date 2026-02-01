import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(MyPlugins.kotlinMultiplatform)
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
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(project(":shared:utils"))
            api(project(":shared:resources:contracts"))
        }
    }
}
android {
    compileSdk = 36
    namespace = "ir.amirab.util.compose"
    defaultConfig {
        minSdk = 26
    }
}
