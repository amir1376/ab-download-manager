plugins {
    id(MyPlugins.kotlin)
}
dependencies {
    api(libs.http4k.core)
    implementation(libs.nanoHttpd.core)
}