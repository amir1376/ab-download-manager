plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeBase)
}
dependencies {
    implementation(project(":downloader:core"))
    api(project(":shared:config"))
    api(project(":shared:utils"))
    api(project(":shared:compose-utils"))
    implementation(libs.androidx.datastore)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
}