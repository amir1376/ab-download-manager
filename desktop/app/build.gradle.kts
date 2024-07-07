import buildlogic.*
import buildlogic.versioning.*
import org.jetbrains.changelog.Changelog
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
    id(Plugins.Kotlin.serialization)
    id(Plugins.buildConfig)
    id(Plugins.changeLog)
    id(Plugins.ksp)
    id(Plugins.aboutLibraries)
//    id(MyPlugins.proguardDesktop)
}

dependencies {
    implementation(libs.decompose)
    implementation(libs.decompose.jbCompose)

    //because we don't have material design, but we use ripple effect
    implementation(libs.compose.material.rippleEffect)

    implementation(libs.koin.core)

    implementation(libs.kotlin.serialization.json)

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.swing)

    implementation(libs.kotlin.datetime)

    implementation(libs.composeFilePicker) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(libs.compose.reorderable)

    implementation(libs.http4k.core)
    implementation(libs.http4k.client.okhttp)

    implementation(libs.arrow.core)
    implementation(libs.arrow.optics)
    ksp(libs.arrow.opticKsp)
    implementation(libs.androidx.datastore)
    implementation(libs.aboutLibraries.core)

    implementation(project(":downloader:core"))
    implementation(project(":downloader:monitor"))

    implementation(project(":integration:server"))
    implementation(project(":desktop:shared"))
    implementation(project(":desktop:tray"))
    implementation(project(":desktop:external-draggable"))
    implementation(project(":desktop:custom-window-frame"))
    implementation(project(":shared:app-utils"))
    implementation(project(":shared:utils"))
    implementation(project(":shared:updater"))
    implementation(project(":shared:auto-start"))
    implementation(project(":shared:nanohttp4k"))
}

aboutLibraries {
    prettyPrint = true
    registerAndroidTasks = false
}

tasks.processResources {
    from(tasks.named("exportLibraryDefinitions"))
}

val desktopPackageName = "com.abdownloadmanager.desktop"
compose {
    desktop {
        application {
//            val getProguardConfigurationsTask = tasks.getProguardConfigurations.get()
            buildTypes.release.proguard {
                isEnabled.set(false)
//                obfuscate.set(false)
//                optimize.set(true)
//                configurationFiles.from(
//                    project.fileTree("proguard"),
//                    getProguardConfigurationsTask.outputs.files.asFileTree.filter {
//                        !it.name.contains("r8")
//                    },
//                )
            }

            // Define the main class for the application.
            mainClass = "$desktopPackageName.AppKt"
            nativeDistributions {
                modules("java.instrument", "jdk.unsupported")
                targetFormats(TargetFormat.Msi, TargetFormat.Deb)
                packageVersion = getAppVersionStringForPackaging()
                packageName = getAppName()
                vendor = "abdownloadmanager.com"
                appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
                val menuGroupName = getPrettifiedAppName()
                linux {
                    debPackageVersion = getAppVersionStringForPackaging(TargetFormat.Deb)
                    rpmPackageVersion = getAppVersionStringForPackaging(TargetFormat.Rpm)
                    iconFile = project.file("icons/icon.png")
                    menuGroup = menuGroupName
                    shortcut = true
                }
                macOS {
                    pkgPackageVersion = getAppVersionStringForPackaging(TargetFormat.Pkg)
                    dmgPackageVersion = getAppVersionStringForPackaging(TargetFormat.Dmg)
                    iconFile = project.file("icons/icon.icns")
                }
                windows {
                    exePackageVersion = getAppVersionStringForPackaging(TargetFormat.Exe)
                    msiPackageVersion = getAppVersionStringForPackaging(TargetFormat.Msi)
                    upgradeUuid = properties["INSTALLER.WINDOWS.UPGRADE_UUID"]?.toString()
                    iconFile = project.file("icons/icon.ico")
                    console = false
                    dirChooser = true
                    shortcut = true
                    menuGroup = menuGroupName
                    menu = true
                }
            }
        }
    }
}


// generate a file with these constants
buildConfig {
    packageName = "$desktopPackageName"
    buildConfigField(
        "PACKAGE_NAME",
        provider {
            getApplicationPackageName()
        }
    )
    buildConfigField(
        "APP_VERSION",
        provider { getAppVersionString() }
    )
    buildConfigField(
        "APP_NAME",
        provider { getPrettifiedAppName() }
    )
    buildConfigField(
        "PROJECT_WEBSITE",
        provider {
            "https://abdownloadmanager.com"
        }
    )
    buildConfigField(
        "PROJECT_SOURCE_CODE",
        provider {
            "https://github.com/amir1376/ABDownloadManager"
        }
    )
}


changelog {
    path.set(rootProject.layout.projectDirectory.dir("CHANGELOG.md").asFile.path)
    version.set(getAppVersionString())
}

// ======= begin of GitHub action stuff
val ciDir = CiDirs(rootProject.layout.buildDirectory)

val createBinariesForCi by tasks.registering {
    val nativeDistributions = compose.desktop.application.nativeDistributions
    val mainRelease = nativeDistributions.outputBaseDir.dir("main-release")
    dependsOn("packageReleaseDistributionForCurrentOS")
    inputs.property("appVersion", getAppVersionString())
    inputs.dir(mainRelease)
    outputs.dir(ciDir.binariesDir)
    doLast {
        val output = ciDir.binariesDir.get().asFile
        output.deleteRecursively()
        val allowedTarget = nativeDistributions.targetFormats.filter { it.isCompatibleWithCurrentOS }
        for (target in allowedTarget) {
            CiUtils.moveAndCreateSignature(
                getAppVersion(),
                nativeDistributions,
                target,
                mainRelease.get().asFile,
                output,
            )
        }
        logger.lifecycle("app packages for '${allowedTarget.joinToString(", ") { it.name }}' written in $output")
    }
}

val createChangeNoteForCi by tasks.registering {
    inputs.property("appVersion", getAppVersionString())
    inputs.file(changelog.path)
    outputs.file(ciDir.changeNotesFile)
    doLast {
        val output = ciDir.changeNotesFile.get().asFile
        val bodyText = with(changelog) {
            getOrNull(getAppVersionString())?.let { item ->
                renderItem(item, Changelog.OutputType.MARKDOWN)
            }
        }.orEmpty()
        logger.lifecycle("changeNotes written in $output")
        output.writeText(bodyText)
    }
}

val createReleaseFolderForCi by tasks.registering {
    dependsOn(createBinariesForCi, createChangeNoteForCi)
}
// ======= end of GitHub action stuff
