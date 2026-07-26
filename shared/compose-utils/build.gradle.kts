

plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(MyPlugins.composeBase)
    id(Plugins.Android.multiplatformLibrary)
}
kotlin {
    jvm("desktop")
    android {
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        namespace = "ir.amirab.util.compose"
        minSdk = 26
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
