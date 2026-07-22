plugins{
    id(MyPlugins.kotlin)
    id(Plugins.Kotlin.serialization)
}

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.ktor.server.cio)
    implementation(project(":shared:utils"))
}
