package ir.amirab.installer

import ir.amirab.installer.extensiion.InstallerPluginExtension
import ir.amirab.installer.tasks.macos.CreateDmgTask
import ir.amirab.installer.tasks.windows.NsisTask
import ir.amirab.installer.utils.Constants
import ir.amirab.util.platform.Platform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class InstallerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("installerPlugin", InstallerPluginExtension::class)
        target.afterEvaluate {
            registerTasks(target, extension)
        }
    }

    private fun registerTasks(
        project: Project,
        extension: InstallerPluginExtension
    ) {
        val windowConfig = extension.windowsConfig
        val macosConfig = extension.macosConfig
        val createInstallerTaskName = Constants.CREATE_INSTALLER_TASK_NAME
        val createInstallerNsisTaskName = "${createInstallerTaskName}Nsis"
        val createInstallerDmgTaskName = "${createInstallerTaskName}Dmg"
        if (windowConfig != null) {
            project.tasks
                .register<NsisTask>(createInstallerNsisTaskName)
                .configure {
                    dependsOn(extension.taskDependencies.toTypedArray())
                    this.nsisTemplate.set(requireNotNull(windowConfig.nsisTemplate) { "Nsis Template not provided" })
                    this.commonParams.set(windowConfig)
                    this.extraParams.set(windowConfig.extraParams)
                    this.destFolder.set(extension.outputFolder.get().asFile)
                    this.outputFileName.set(requireNotNull(windowConfig.outputFileName) { " outputFileName not provided " })
                    this.sourceFolder.set(requireNotNull(windowConfig.inputDir) { "inputDir not provided" })
                }
        }
        if (macosConfig != null) {
            project.tasks
                .register<CreateDmgTask>(createInstallerDmgTaskName)
                .configure {
                    dependsOn(extension.taskDependencies.toTypedArray())
                    this.appName.set(requireNotNull(macosConfig.appName) { "appName not provided" })
                    this.appFileName.set(requireNotNull(macosConfig.appFileName) { "iconFile not provided" })
                    this.backgroundImage.set(requireNotNull(macosConfig.backgroundImage) { "backgroundImage not provided" })
                    this.outputFileName.set(requireNotNull(macosConfig.outputFileName) { "outputFileName not provided" })
                    this.inputDir.set(requireNotNull(macosConfig.inputDir) { "inputDir not provided" })
                    this.destFolder.set(extension.outputFolder.get().asFile)
                    this.iconSize.set(macosConfig.iconSize)
                    this.windowHeight.set(macosConfig.windowHeight)
                    this.windowWidth.set(macosConfig.windowWidth)
                    this.folderOffsetX.set(macosConfig.folderOffsetX)
                    this.appOffsetX.set(macosConfig.appOffsetX)
                    this.iconsY.set(macosConfig.iconsY)
                    this.licenseFile.set(macosConfig.licenseFile)
                }
        }
        project.tasks.register(createInstallerTaskName) {
            // when we want to create installer we need to prepare its input first!
            when (val platform = Platform.getCurrentPlatform()) {
                Platform.Desktop.Linux -> {
                    // nothing yet
                }

                Platform.Desktop.MacOS -> {
                    if (macosConfig != null) {
                        dependsOn(createInstallerDmgTaskName)
                    }
                }

                Platform.Desktop.Windows -> {
                    if (windowConfig != null) {
                        dependsOn(createInstallerNsisTaskName)
                    }
                }

                else -> error("unsupported platform: $platform")
            }
        }
    }
}