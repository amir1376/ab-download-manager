plugins{
    id(MyPlugins.kotlin)
    id(Plugins.Kotlin.serialization)
}

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    implementation(project(":shared:utils"))
    implementation(project(":shared:nanohttp4k"))
}