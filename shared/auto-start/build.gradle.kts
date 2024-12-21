plugins {
    id(MyPlugins.kotlin)
}
dependencies {
    implementation(project(":shared:utils"))
    // for windows, we use registry
    implementation(libs.jna.platform)
}