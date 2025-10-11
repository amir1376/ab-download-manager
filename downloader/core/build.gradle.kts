plugins {
    id(MyPlugins.kotlin)
    id(Plugins.Kotlin.serialization)
}
dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlin.coroutines.core)
    api(libs.okio.okio)
    api(libs.okhttp.okhttp)
    api(libs.okhttp.coroutines)
    implementation(project(":shared:utils"))
    api("io.lindstrom:m3u8-parser:0.29")

}
