plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
}
dependencies {
    api(project(":desktop:shared"))
    api(project(":shared:app"))
    implementation(libs.jbrApi)
}
