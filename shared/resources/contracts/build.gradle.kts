plugins {
    id(MyPlugins.kotlinMultiplatform)
    id(Plugins.Android.multiplatformLibrary)
}
kotlin {
    jvm("desktop")
    android {
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        namespace = "com.abdownloadmanager.resources.contracts"
        minSdk = 26
    }
    sourceSets.commonMain.dependencies {
        implementation(libs.okio.okio)
    }
}
