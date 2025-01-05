plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
}
dependencies {
    api(project(":desktop:custom-window-frame"))
    api(project(":desktop:shared"))
    api(project(":shared:app"))
}