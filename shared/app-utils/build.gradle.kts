plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeBase)
    id(Plugins.Kotlin.serialization)
}
dependencies {
    api(project(":downloader:core"))
    api(project(":downloader:monitor"))
    api(project(":shared:config"))
    api(project(":shared:utils"))
    api(project(":shared:compose-utils"))
    api(project(":shared:resources"))

    api(libs.decompose)

    api(libs.androidx.datastore)
    api(libs.kotlin.coroutines.core)
    api(libs.kotlin.serialization.json)
    implementation(libs.kotlinFileWatcher)

    api(libs.koin.core)

    api(compose.runtime)
    api(compose.foundation)
    api(compose.ui)

    //because we don't have material design, but we use ripple effect
    implementation(libs.compose.material.rippleEffect)
}