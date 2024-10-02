package ir.amirab.installer

import ir.amirab.installer.extensiion.InstallerPluginExtension
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
        val createInstallerTaskName = Constants.CREATE_INSTALLER_TASK_NAME
        val createInstallerNsisTaskName = "${createInstallerTaskName}Nsis"
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
        project.tasks.register(createInstallerTaskName) {
            // when we want to create installer we need to prepare its input first!
            when (val platform = Platform.getCurrentPlatform()) {
                Platform.Desktop.Linux -> {
                    // nothing yet
                }

                Platform.Desktop.MacOS -> {
                    // nothing yet
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