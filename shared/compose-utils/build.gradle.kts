plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeBase)
}
dependencies {
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(project(":shared:utils"))
}