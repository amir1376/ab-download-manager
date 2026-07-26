
plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Android.multiplatformLibrary)
}
kotlin {
    jvm("desktop")
    android {
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        namespace = "ir.amirab.util.startup"
        minSdk = 26
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
