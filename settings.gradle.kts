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
include("desktop:app-utils")
include("desktop:shared")
include("desktop:tray:common")
include("desktop:tray:windows")
include("desktop:tray:linux")
include("desktop:tray:mac")
include("desktop:mac_utils")
include("downloader:core")
include("downloader:monitor")
include("integration:server")
include("shared:utils")
include("shared:app")
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
