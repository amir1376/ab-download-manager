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
    }
}

rootProject.name = "ABDownloadManager"

include("desktop:app")
include("desktop:custom-window-frame")
include("desktop:shared")
include("desktop:tray")
include("downloader:core")
include("downloader:monitor")
include("integration:server")
include("shared:utils")
include("shared:app-utils")
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
