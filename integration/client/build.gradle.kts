plugins{
    id(MyPlugins.kotlin)
    id(Plugins.Kotlin.serialization)
}

dependencies {
    implementation(project(":integration:server"))
    implementation(project(":shared:config"))
    implementation(libs.kotlin.serialization.json)
    implementation(libs.http4k.core)
    implementation(libs.http4k.client.okhttp)
}