import buildlogic.*
import buildlogic.versioning.*
import ir.amirab.installer.InstallerTargetFormat
import org.jetbrains.changelog.Changelog
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import ir.amirab.util.platform.Platform
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*

plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
    id(Plugins.Kotlin.serialization)
    id(Plugins.buildConfig)
    id(Plugins.changeLog)
    id(Plugins.ksp)
    id(Plugins.aboutLibraries)
    id("ir.amirab.installer-plugin")
//    id(MyPlugins.proguardDesktop)
}


dependencies {
    implementation(libs.decompose)
    implementation(libs.decompose.jbCompose)

    implementation(libs.koin.core)

    implementation(libs.kotlin.serialization.json)

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.swing)

    implementation(libs.kotlin.datetime)

    implementation(libs.compose.reorderable)

    implementation(libs.http4k.core)
    implementation(libs.http4k.client.okhttp)

    implementation(libs.arrow.core)
    implementation(libs.arrow.optics)
    ksp(libs.arrow.opticKsp)


    implementation(libs.androidx.datastore)

    implementation(libs.aboutLibraries.core)
    implementation(libs.markdownRenderer.core)
    implementation(libs.composeFileKit) {
        exclude(group = "net.java.dev.jna")
    }
    implementation(libs.osThemeDetector) {
        exclude(group = "net.java.dev.jna")
    }
    implementation(libs.proxyVole) {
        exclude(group = "net.java.dev.jna")
    }
    implementation(libs.jna.core)
    implementation(libs.jna.platform)

    implementation(project(":downloader:core"))
    implementation(project(":downloader:monitor"))

    implementation(project(":integration:server"))
    implementation(project(":desktop:shared"))
    implementation(project(":desktop:app-utils"))

    implementation(project(":desktop:tray:common"))

    // Detection based on the operating system
    when (Platform.getCurrentPlatform()) {
        Platform.Desktop.Windows -> {
            implementation(project(":desktop:tray:windows"))
        }

        Platform.Desktop.Linux -> {
            implementation(project(":desktop:tray:linux"))
        }

        Platform.Desktop.MacOS -> {
            implementation(project(":desktop:tray:mac"))
        }

        else -> Unit
    }

    implementation(project(":shared:app"))
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
                targetFormats(Msi, Deb)
                if (Platform.getCurrentPlatform() == Platform.Desktop.Linux) {
                    // filekit library requires this module in linux.
                    modules("jdk.security.auth")
                }
                packageVersion = getAppVersionStringForPackaging()
                packageName = getAppName()
                vendor = "abdownloadmanager.com"
                appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
                val menuGroupName = getPrettifiedAppName()
                licenseFile.set(rootProject.file("LICENSE"))
                linux {
                    debPackageVersion = getAppVersionStringForPackaging(Deb)
                    rpmPackageVersion = getAppVersionStringForPackaging(Rpm)
                    appCategory = "Network"
                    iconFile = project.file("icons/icon.png")
                    menuGroup = menuGroupName
                    shortcut = true
                }
                macOS {
                    pkgPackageVersion = getAppVersionStringForPackaging(Pkg)
                    dmgPackageVersion = getAppVersionStringForPackaging(Dmg)
                    iconFile = project.file("icons/icon.icns")
                }
                windows {
                    exePackageVersion = getAppVersionStringForPackaging(Exe)
                    msiPackageVersion = getAppVersionStringForPackaging(Msi)
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

installerPlugin {
    dependsOn("createReleaseDistributable")
    outputFolder.set(layout.buildDirectory.dir("custom-installer"))
    windows {
        appName = getAppName()
        appDisplayName = getPrettifiedAppName()
        appVersion = getAppVersionStringForPackaging(Exe)
        appDisplayVersion = getAppVersionString()
        appDataDirName = getAppDataDirName()
        inputDir = project.file("build/compose/binaries/main-release/app/${getAppName()}")
        outputFileName = getAppName()
        licenceFile = rootProject.file("LICENSE")
        iconFile = project.file("icons/icon.ico")
        nsisTemplate = project.file("resources/installer/nsis-script-template.nsi")
        extraParams = mapOf(
            "app_publisher" to "abdownloadmanager.com",
            "app_version_with_build" to "${getAppVersionStringForPackaging(Exe)}.0",
            "source_code_url" to "https://github.com/amir1376/ab-download-manager",
            "project_website" to "www.abdownloadmanager.com",
            "copyright" to "© 2024-present AB Download Manager App",
            "header_image_file" to project.file("resources/installer/abdm-header-image.bmp"),
            "sidebar_image_file" to project.file("resources/installer/abdm-sidebar-image.bmp")
        )

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
        "APP_DISPLAY_NAME",
        provider { getPrettifiedAppName() }
    )
    buildConfigField(
        "DATA_DIR_NAME",
        provider { getAppDataDirName() }
    )
    buildConfigField(
        "APP_VERSION",
        provider { getAppVersionString() }
    )
    buildConfigField(
        "APP_NAME",
        provider { getAppName() }
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
            "https://github.com/amir1376/ab-download-manager"
        }
    )
    buildConfigField(
        "PROJECT_GITHUB_OWNER",
        provider {
            "amir1376"
        }
    )
    buildConfigField(
        "PROJECT_GITHUB_REPO",
        provider {
            "ab-download-manager"
        }
    )
    buildConfigField(
        "PROJECT_TRANSLATIONS",
        provider {
            "https://crowdin.com/project/ab-download-manager"
        }
    )
    buildConfigField(
        "INTEGRATION_CHROME_LINK",
        provider {
            "https://chromewebstore.google.com/detail/ab-download-manager-brows/bbobopahenonfdgjgaleledndnnfhooj"
        }
    )
    buildConfigField(
        "INTEGRATION_FIREFOX_LINK",
        provider {
            "https://addons.mozilla.org/en-US/firefox/addon/ab-download-manager/"
        }
    )
    buildConfigField(
        "TELEGRAM_GROUP",
        provider {
            "https://t.me/abdownloadmanager_discussion"
        }
    )
    buildConfigField(
        "TELEGRAM_CHANNEL",
        provider {
            "https://t.me/abdownloadmanager"
        }
    )
}


changelog {
    path.set(rootProject.layout.projectDirectory.dir("CHANGELOG.md").asFile.path)
    version.set(getAppVersionString())
}

// ======= begin of GitHub action stuff
val ciDir = CiDirs(rootProject.layout.buildDirectory)

val appPackageNameByComposePlugin
    get() = requireNotNull(compose.desktop.application.nativeDistributions.packageName) {
        "compose.desktop.application.nativeDistributions.packageName must not be null!"
    }

val distributableAppArchiveDir: Provider<Directory> = project.layout.buildDirectory.dir("dist/archives")
fun AbstractArchiveTask.fromAppImagePath() {
    from(tasks.named("createReleaseDistributable"))
    destinationDirectory.set(distributableAppArchiveDir)
}

val createDistributableAppArchiveTar by tasks.registering(Tar::class) {
    archiveFileName.set("app.tar.gz")
    compression = Compression.GZIP
    fromAppImagePath()
}
val createDistributableAppArchiveZip by tasks.registering(Zip::class) {
    archiveFileName.set("app.zip")
    fromAppImagePath()
}
val createDistributableAppArchive by tasks.registering {
    when (Platform.getCurrentPlatform()) {
        Platform.Desktop.Linux,
        Platform.Desktop.MacOS -> dependsOn(createDistributableAppArchiveTar)

        Platform.Desktop.Windows -> dependsOn(createDistributableAppArchiveZip)
        Platform.Android -> error("this task is used for desktop only")
    }
}

val createBinariesForCi by tasks.registering {
    val nativeDistributions = compose.desktop.application.nativeDistributions
    val mainRelease = nativeDistributions.outputBaseDir.dir("main-release")
    if (installerPlugin.isThisPlatformSupported()) {
        dependsOn(installerPlugin.createInstallerTask)
        inputs.dir(installerPlugin.outputFolder)
    } else {
        dependsOn("packageReleaseDistributionForCurrentOS")
        inputs.dir(mainRelease)
    }
    dependsOn(createDistributableAppArchive)
    inputs.property("appVersion", getAppVersionString())
    inputs.dir(distributableAppArchiveDir)
    outputs.dir(ciDir.binariesDir)
    doLast {
        val output = ciDir.binariesDir.get().asFile
        val packageName = appPackageNameByComposePlugin
        output.deleteRecursively()

        if (installerPlugin.isThisPlatformSupported()) {
            val targets = installerPlugin.getCreatedInstallerTargetFormats()
            for (target in targets) {
                CiUtils.movePackagedAndCreateSignature(
                    getAppVersion(),
                    packageName,
                    target,
                    installerPlugin.outputFolder.get().asFile,
                    output,
                )
            }
            logger.lifecycle("app packages for '${targets.joinToString(", ") { it.name }}' written in $output using the installer plugin")
        } else {
            val allowedTargets = nativeDistributions
                .targetFormats.filter { it.isCompatibleWithCurrentOS }
                .map {
                    it.toInstallerTargetFormat()
                }
            for (target in allowedTargets) {
                CiUtils.movePackagedAndCreateSignature(
                    getAppVersion(),
                    packageName,
                    target,
                    mainRelease.get().asFile.resolve(target.outputDirName),
                    output,
                )
            }
            logger.lifecycle("app packages for '${allowedTargets.joinToString(", ") { it.name }}' written in $output using compose packager tool")
        }
        val appArchiveDistributableDir = distributableAppArchiveDir.get().asFile
        CiUtils.copyAndHashToDestination(
            distributableAppArchiveDir.get().asFile.resolve(
                CiUtils.getFileOfDistributedArchivedTarget(
                    appArchiveDistributableDir,
                )
            ),
            output,
            CiUtils.getTargetFileName(
                packageName,
                getAppVersion(),
                null, // this is not an installer (it will be automatically converted to current os name
            )
        )
        logger.lifecycle("distributable app archive written in ${output}")
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

fun TargetFormat.toInstallerTargetFormat(): InstallerTargetFormat {
    return when (this) {
        AppImage -> error("$this is not recognized as installer")
        Deb -> InstallerTargetFormat.Deb
        Rpm -> InstallerTargetFormat.Rpm
        Dmg -> InstallerTargetFormat.Dmg
        Pkg -> InstallerTargetFormat.Pkg
        Exe -> InstallerTargetFormat.Exe
        Msi -> InstallerTargetFormat.Msi
    }
}
