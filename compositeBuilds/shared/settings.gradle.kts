dependencyResolutionManagement{
    versionCatalogs {
        create("libs"){
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "shared-code-between-gradle-and-app"
include("platform")