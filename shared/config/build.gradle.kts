plugins {
    id(MyPlugins.kotlin)
}
dependencies {
    implementation(libs.androidx.datastore)
    implementation(libs.kotlin.serialization.json)
}