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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(project(":shared:utils"))
            api(project(":shared:resources:contracts"))
            api("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("io.coil-kt.coil3:coil-svg:3.3.0")
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
