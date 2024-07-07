plugins{
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
}
dependencies{
    implementation(project(":desktop:shared"))
}