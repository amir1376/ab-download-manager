plugins {
    id(MyPlugins.kotlin)
    id(Plugins.Kotlin.serialization)
}
dependencies {
    implementation(libs.kotlin.serialization.json)
    api(libs.okhttp.okhttp)
    api(libs.kotlin.coroutines.core)
    implementation(project(":shared:utils"))
    implementation(libs.jna.platform)
    implementation(libs.semver)
}