package com.abdownloadmanager.updateapplier

import ir.amirab.util.platform.Platform
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.sink
import okio.use
import java.io.File

/**
 * the duty of the script is
 * it accepts [updateFolder], [installationFolder]
 * 1. stop the app
 * 2. remove the installed app files
 * 3. copy [updateFolder] into [installationFolder]
 * 4. remove [updateFolder]
 * 5. start the app again
 */
class UpdateInstallerByScript(
    private val installationFolder: String,
    private val updateFolder: String,
    private val logDir: String,
) : UpdateInstaller {
    private fun getScriptPath(logFile: String): String {
        val platform = Platform.getCurrentPlatform()
        val scriptForPlatform = when (platform) {
            Platform.Desktop.Linux -> {
                "com/abdownloadmanager/updater/updater_linux.sh"
            }

            Platform.Desktop.Windows -> {
                "com/abdownloadmanager/updater/updater_windows.bat"
            }

            else -> error("script for this platform not found")
        }.toPath()
        val scriptExtension = scriptForPlatform.toString().substringAfterLast('.', "")
        val scriptContent = FileSystem.RESOURCES.source(scriptForPlatform).buffer().use {
            it.readUtf8()
        }
        val scriptPathInTempFolder = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve(
            "abdm-updater.$scriptExtension"
        )
        scriptPathInTempFolder.toFile().writeText(scriptContent)
        val scriptContentFile = scriptPathInTempFolder.toString()
        val commandToRun = when (platform) {
            Platform.Desktop.Linux -> execInBash(
                scriptPath = scriptContentFile,
                updateFolder = updateFolder,
                installationFolder = installationFolder,
                logFile = logFile,
            )

            Platform.Desktop.MacOS -> execInBash(
                scriptPath = scriptContentFile,
                updateFolder = updateFolder,
                installationFolder = installationFolder,
                logFile = logFile,
            )

            Platform.Desktop.Windows -> execInCMD(
                scriptPath = scriptContentFile,
                updateFolder = updateFolder,
                installationFolder = installationFolder,
                logFile = logFile,
            )

            else -> error("platform ${platform} not supported")
        }
        val scriptToRun = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("abdm-updater.run.$scriptExtension")
        scriptToRun.toFile().writeText(commandToRun)
        return scriptToRun.toString()
    }

    private fun executeScript() {
        val logFile = File(logDir, "update_log.txt")
            .apply {
                parentFile.mkdirs()
            }.path
        val scriptPath = getScriptPath(logFile)


        val command = when (val p = Platform.getCurrentPlatform()) {
            Platform.Desktop.Linux -> arrayOf("bash", scriptPath)
            Platform.Desktop.MacOS -> arrayOf("bash", scriptPath)
            Platform.Desktop.Windows -> arrayOf("cmd", "/c", scriptPath)
            else -> error("platform: $p not supported for updating by script")
        }
//        println("execute script $command")
        ProcessBuilder()
            .command(*command)
            .apply {
                // in linux if I don't remove it the program won't restart
                environment().remove("_JPACKAGE_LAUNCHER")
            }
            .start()
    }

    private fun execInCMD(
        scriptPath: String,
        updateFolder: String,
        installationFolder: String,
        logFile: String,
    ): String {
        return """
            cmd /c ""${scriptPath}" "${updateFolder}" "${installationFolder}" > "${logFile}" 2>&1"
        """.trimIndent()
    }

    private fun execInBash(
        scriptPath: String,
        updateFolder: String,
        installationFolder: String,
        logFile: String,
    ): String {
        return """
            bash "${scriptPath}" "${updateFolder}" "${installationFolder}" > "${logFile}" 2>&1 &
        """.trimIndent()
    }

    override fun installUpdate() {
        executeScript()
    }
}