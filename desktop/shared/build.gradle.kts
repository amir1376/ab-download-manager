plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
}

dependencies {
    // Jna
    implementation(libs.jna.core)

    implementation(project(":shared:app"))
    implementation(project(":shared:app-utils"))
    implementation(project(":shared:utils"))
}