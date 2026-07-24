plugins{
    id(MyPlugins.kotlin)
    id(Plugins.Kotlin.serialization)
}

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.auth.core)
    implementation(libs.ktor.server.auth.apiKey)
    implementation(project(":shared:utils"))
}
