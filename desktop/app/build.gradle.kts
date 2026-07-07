import buildlogic.*
import buildlogic.versioning.*
import ir.amirab.util.platform.Platform
import com.mikepenz.aboutlibraries.plugin.DuplicateMode
import com.mikepenz.aboutlibraries.plugin.DuplicateRule
import dev.nucleusframework.desktop.application.dsl.TargetFormat
import ir.amirab.util.platform.Arch
import ir.amirab.util.platform.isLinux

plugins {
    id(MyPlugins.kotlin)
    id(MyPlugins.composeDesktop)
    id(Plugins.Kotlin.serialization)
    id(Plugins.ksp)
    id(Plugins.aboutLibraries)
    id(Plugins.kotlinRpc)
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

    implementation(libs.arrow.core)
    implementation(libs.arrow.optics)
    ksp(libs.arrow.opticKsp)

    implementation(libs.androidx.datastore)

    implementation(libs.aboutLibraries.core)
    implementation(libs.markdownRenderer.core)
    implementation(libs.composeFileKit) {
        exclude(group = "net.java.dev.jna")
    }
    implementation(libs.proxyVole) {
        exclude(group = "net.java.dev.jna")
    }
    implementation(libs.jna.core)
    implementation(libs.jna.platform)

    implementation(libs.kotlin.reflect) // used by kotlin-rpc
    implementation(libs.kotlinx.rpc.client)
    implementation(libs.kotlinx.rpc.client.ktor)
    implementation(libs.kotlinx.rpc.server)
    implementation(libs.kotlinx.rpc.server.ktor)
    implementation(libs.kotlinx.rpc.serializationJson)

    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.server.cio)

    implementation(project(":downloader:core"))
    implementation(project(":downloader:monitor"))

    implementation(project(":integration:server"))
    implementation(project(":desktop:shared"))
    implementation(project(":desktop:app-utils"))

    implementation(libs.composeNativeTray)
    implementation(libs.nucleus.aot)

    implementation(project(":shared:app"))
    implementation(project(":shared:utils"))
    implementation(project(":shared:updater"))
    implementation(project(":desktop:mac_utils"))
    implementation(project(":desktop:slf4j-impl"))
}

aboutLibraries {
    export {
        prettyPrint = true
    }
    library {
        duplicationMode = DuplicateMode.MERGE
        duplicationRule = DuplicateRule.SIMPLE
    }
}

tasks.processResources {
    from(tasks.named("exportLibraryDefinitions"))
}

val desktopPackageName = "com.abdownloadmanager.desktop"
nucleus {
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
            cleanupNativeLibs = true
            // enable it later
            enableAotCache = false
            modules(
                "java.instrument",
                "jdk.unsupported",
                "jdk.accessibility",
            )
            if (Platform.isLinux()) {
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
                appCategory = "Network"
                iconFile = project.file("icons/icon.png")
                menuGroup = menuGroupName
                shortcut = true
            }
            macOS {
                iconFile = project.file("icons/icon.icns")
                infoPlist {
                    extraKeysRawXml = """
                            <key>LSUIElement</key>
                            <string>true</string>
                        """.trimIndent()
                }
                jvmArgs("-Dapple.awt.enableTemplateImages=true")
            }
            windows {
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

installerPlugin {
    dependsOn("createReleaseDistributable")
    outputFolder.set(layout.buildDirectory.dir("custom-installer"))
    windows {
        appName = getAppName()
        appDisplayName = getPrettifiedAppName()
        appVersion = getAppVersionStringForPackaging(TargetFormat.Exe)
        appDisplayVersion = getAppVersionString()
        appDataDirName = getAppDataDirName()
        inputDir = project.file("build/compose/binaries/main-release/app/${getAppName()}")
        outputFileName = getAppName()
        licenceFile = rootProject.file("LICENSE")
        iconFile = project.file("icons/icon.ico")
        nsisTemplate = project.file("resources/installer/nsis-script-template.nsi")
        extraParams = mapOf(
            "app_publisher" to "abdownloadmanager.com",
            "app_version_with_build" to "${getAppVersionStringForPackaging(TargetFormat.Exe)}.0",
            "source_code_url" to "https://github.com/amir1376/ab-download-manager",
            "project_website" to "www.abdownloadmanager.com",
            "copyright" to "© 2024-present AB Download Manager App",
            "header_image_file" to project.file("resources/installer/abdm-header-image.bmp"),
            "sidebar_image_file" to project.file("resources/installer/abdm-sidebar-image.bmp")
        )
    }
    macos {
        appName = getAppName()
        inputDir = project.file("build/compose/binaries/main-release/app/")
        appFileName = "${getAppName()}.app"
        backgroundImage = project.file("resources/installer/dmg_background.png")
        outputFileName = getAppName()
        licenseFile = rootProject.file("LICENSE")
        volumeIcon = project.file("icons/icon.icns")
    }
}

fun Task.dependsOnAOT() {
    if (nucleus.application.nativeDistributions.enableAotCache) {
        dependsOn("generateReleaseAotCache")
    }
}

val postReleaseDistributable by tasks.registering {
    dependsOn("createReleaseDistributable")
    description = "Any modification need to be added to the app distributable folder, should be added here"
    dependsOnAOT()
}
installerPlugin.dependsOn(postReleaseDistributable)

// ======= begin of GitHub action stuff
val ciDir = CiUtils.getCiDir(project)

val appPackageNameByComposePlugin
    get() = requireNotNull(nucleus.application.nativeDistributions.packageName) {
        "compose.desktop.application.nativeDistributions.packageName must not be null!"
    }

val distributableAppArchiveDir: Provider<Directory> =
    project.layout.buildDirectory.dir("dist/archives")

fun AbstractArchiveTask.fromAppImagePath() {
    from(tasks.named("createReleaseDistributable"))
    destinationDirectory.set(distributableAppArchiveDir)
}

/**
 * gradle 9 removes file permissions and timestamp by default in archive tasks!. but we want them!
 */
fun AbstractArchiveTask.preserveFileAttributes() {
    // Make file order based on the file system
    isReproducibleFileOrder = false
    // Use file timestamps from the file system
    isPreserveFileTimestamps = true
    // Use permissions from the file system
    useFileSystemPermissions()
}

val createDistributableAppArchiveTar by tasks.registering(Tar::class) {
    dependsOn(postReleaseDistributable)
    preserveFileAttributes()
    archiveFileName.set("app.tar.gz")
    compression = Compression.GZIP
    fromAppImagePath()
}
val createDistributableAppArchiveZip by tasks.registering(Zip::class) {
    dependsOn(postReleaseDistributable)
    preserveFileAttributes()
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

tasks.register(CiUtils.getCreateBinaryFolderForCiTaskName()) {
    if (installerPlugin.isThisPlatformSupported()) {
        dependsOn(installerPlugin.createInstallerTask)
        inputs.dir(installerPlugin.outputFolder)
    }
    dependsOn(createDistributableAppArchive)
    inputs.property("appVersion", getAppVersionString())
    inputs.dir(distributableAppArchiveDir)
    outputs.dir(ciDir.binariesDir)
    doLast {
        val output = ciDir.binariesDir.get().asFile
        val packageName = appPackageNameByComposePlugin

        if (installerPlugin.isThisPlatformSupported()) {
            val targets = installerPlugin.getCreatedInstallerTargetFormats()
            for (target in targets) {
                CiUtils.movePackagedAndCreateSignature(
                    appVersion = getAppVersion(),
                    packageName = packageName,
                    target = target,
                    basePackagedAppsDir = installerPlugin.outputFolder.get().asFile,
                    outputDir = output,
                )
            }
            logger.lifecycle("app packages for '${targets.joinToString(", ") { it.name }}' written in $output using the installer plugin")
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
                Arch.getCurrentArch().name
            )
        )
        logger.lifecycle("distributable app archive written in ${output}")
    }
}
// ======= end of GitHub action stuff
