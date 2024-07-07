plugins {
    id(MyPlugins.kotlin)
    id(Plugins.Kotlin.serialization)
    id(MyPlugins.composeDesktop)
}
dependencies {
    implementation(project(":downloader:core"))
    implementation(project(":shared:utils"))
    implementation(libs.kotlin.coroutines.core)
    implementation(compose.runtime)
}