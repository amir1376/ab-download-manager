plugins{
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
}
dependencies{
    api(project(":desktop:shared"))
    api(project(":desktop:app-utils"))
}
