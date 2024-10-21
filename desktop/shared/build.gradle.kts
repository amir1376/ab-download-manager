plugins{
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
}

dependencies {
    implementation(project(":shared:app-utils"))
    implementation(project(":shared:utils"))
}