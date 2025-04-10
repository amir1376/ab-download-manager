plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
}
dependencies {
    api(project(":desktop:shared"))
    api(project(":shared:app"))
    implementation("org.jetbrains.runtime:jbr-api:1.5.0")
}
