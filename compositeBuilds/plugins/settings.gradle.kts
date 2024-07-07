dependencyResolutionManagement{
    versionCatalogs {
        create("libs"){
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}
include("git-version-plugin")