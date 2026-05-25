plugins {
    id(MyPlugins.kotlin)
    id(Plugins.ksp)
}

dependencies {
    implementation(project(":shared:utils"))
    implementation(libs.slf4j.api)
    ksp(libs.autoService.ksp)
    implementation(libs.autoService.annoations)
}
