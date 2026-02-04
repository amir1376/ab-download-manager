pluginManagement {
}

plugins{
//    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://packages.jetbrains.team/maven/p/kpm/public/")
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/")
        maven("https://www.jetbrains.com/intellij-repository/releases")
    }
}

rootProject.name = "ABDownloadManager"

include("android:app")
include("desktop:app")
include("desktop:app-utils")
include("desktop:jewel")
include("desktop:shared")
include("desktop:mac_utils")
include("downloader:core")
include("downloader:monitor")
include("integration:server")
include("shared:utils")
include("shared:app")
include("shared:compose-utils")
include("shared:resources")
include("shared:resources:contracts")
include("shared:config")
include("shared:updater")
include("shared:auto-start")
include("shared:nanohttp4k")
includeBuild("./compositeBuilds/shared"){
    name="build-shared"
}
includeBuild("./compositeBuilds/plugins")
