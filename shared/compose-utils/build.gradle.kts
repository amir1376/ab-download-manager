plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeBase)
}
dependencies {
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(project(":shared:utils"))
    api(project(":shared:resources:contracts"))
}