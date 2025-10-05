plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeBase)
    id(Plugins.Kotlin.serialization)
}
dependencies {
    api(project(":shared:app-utils"))
    api(libs.markdownRenderer.core)
    api(libs.compose.reorderable)
    api(libs.composeFileKit)
}
