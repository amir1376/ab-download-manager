package ir.amirab.installer.extensiion

import ir.amirab.installer.InstallerTargetFormat
import ir.amirab.installer.utils.Constants
import ir.amirab.util.platform.Platform
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.io.Serializable
import javax.inject.Inject

abstract class InstallerPluginExtension {
    @get:Inject
    internal abstract val project: Project

    abstract val outputFolder: DirectoryProperty

    internal val taskDependencies = mutableListOf<Any>()

    fun dependsOn(vararg tasks: Any) {
        taskDependencies.addAll(tasks)
    }

    internal var windowsConfig: WindowsConfig? = null
        private set

    internal var macosConfig: MacosConfig? = null
        private set

    fun windows(
        config: WindowsConfig.() -> Unit
    ) {
        if (Platform.getCurrentPlatform() != Platform.Desktop.Windows) return
        val windowsConfig = if (this.windowsConfig == null) {
            WindowsConfig().also {
                this.windowsConfig = it
            }
        } else {
            this.windowsConfig!!
        }
        windowsConfig.config()
    }

    fun macos(
        config: MacosConfig.() -> Unit
    ) {
        if (Platform.getCurrentPlatform() != Platform.Desktop.MacOS) return
        val macosConfig = if (this.macosConfig == null) {
            MacosConfig().also {
                this.macosConfig = it
            }
        } else {
            this.macosConfig!!
        }
        macosConfig.config()
    }

    val createInstallerTask: TaskProvider<Task> by lazy {
        project.tasks.named(Constants.CREATE_INSTALLER_TASK_NAME)
    }

    fun isThisPlatformSupported() = when (Platform.getCurrentPlatform()) {
        Platform.Desktop.Windows -> windowsConfig != null
        Platform.Desktop.MacOS -> macosConfig != null
        else -> false
    }

    fun getCreatedInstallerTargetFormats(): List<InstallerTargetFormat> {
        return buildList<InstallerTargetFormat> {
            when (Platform.getCurrentPlatform()) {
                Platform.Desktop.Windows -> {
                    if (windowsConfig != null) {
                        add(InstallerTargetFormat.Exe)
                    }
                }

                Platform.Desktop.MacOS -> {
                    if (macosConfig != null) {
                        add(InstallerTargetFormat.Dmg)
                    }
                }

                else -> {}
            }
        }
    }
}

data class WindowsConfig(
    var appName: String? = null,
    var appDisplayName: String? = null,
    var appVersion: String? = null,
    var appDisplayVersion: String? = null,
    var appDataDirName: String? = null,
    var iconFile: File? = null,
    var licenceFile: File? = null,

    var outputFileName: String? = null,

    var inputDir: File? = null,

    var nsisTemplate: File? = null,

    var extraParams: Map<String, Any> = emptyMap()
) : Serializable


data class MacosConfig(
    var appName: String? = null,
    var appFileName: String? = null,
    var outputFileName: String? = null,
    var inputDir: File? = null,
    var backgroundImage: File? = null,
    var iconSize: Int = 100,
    var licenseFile: File? = null,
    val windowWidth: Int = 600,
    val windowHeight: Int = 400,
    val iconsY: Int = 150,
    val appOffsetX: Int = 100,
    val folderOffsetX: Int = 450,
) : Serializable