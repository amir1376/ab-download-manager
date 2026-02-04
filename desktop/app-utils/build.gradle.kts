plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
}
dependencies {
    api(project(":desktop:shared"))
    api(project(":shared:app"))
    api(project(":desktop:jewel-decorated-window"))
    implementation(libs.jbrApi)
}
