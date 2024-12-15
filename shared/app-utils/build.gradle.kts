plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeBase)
    id(Plugins.Kotlin.serialization)
}
dependencies {
    implementation(project(":downloader:core"))
    implementation(project(":downloader:monitor"))
    api(project(":shared:config"))
    api(project(":shared:utils"))
    api(project(":shared:compose-utils"))
    implementation(libs.androidx.datastore)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlinFileWatcher)

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
}