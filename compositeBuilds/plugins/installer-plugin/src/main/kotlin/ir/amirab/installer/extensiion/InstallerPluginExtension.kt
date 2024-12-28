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

    val createInstallerTask: TaskProvider<Task> by lazy {
        project.tasks.named(Constants.CREATE_INSTALLER_TASK_NAME)
    }

    fun isThisPlatformSupported() = when (Platform.getCurrentPlatform()) {
        Platform.Desktop.Windows -> windowsConfig != null
        else -> {
            false
        }
    }

    fun getCreatedInstallerTargetFormats(): List<InstallerTargetFormat> {
        return buildList<InstallerTargetFormat> {
            when (Platform.getCurrentPlatform()) {
                Platform.Desktop.Windows -> {
                    if (windowsConfig != null) {
                        add(InstallerTargetFormat.Exe)
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
    var iconFile: File? = null,
    var licenceFile: File? = null,

    var outputFileName: String? = null,

    var inputDir: File? = null,

    var nsisTemplate: File? = null,

    var extraParams: Map<String, Any> = emptyMap()
) : Serializable

