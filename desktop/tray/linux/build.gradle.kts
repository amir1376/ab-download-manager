plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
    id(Plugins.ksp)
}
dependencies {
    api(project(":desktop:tray:common"))
    ksp(libs.autoService.ksp)
    implementation(libs.autoService.annoations)
    implementation(libs.systemTray)
}