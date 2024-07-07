plugins {
    id(MyPlugins.kotlin)
    id(Plugins.Kotlin.serialization)
}
dependencies {
    implementation(libs.kotlin.serialization.json)
    api(libs.okhttp.okhttp)
    api(libs.kotlin.coroutines.core)
    api(libs.kotlin.datetime)
    api(libs.semver)
    api(libs.arrow.optics)
    api("ir.amirab.util:platform:1")
}