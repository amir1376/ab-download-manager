package com.abdownloadmanager.updateapplier

import ir.amirab.util.platform.Platform
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
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
    private fun getScriptPath(): String {
        val scriptForPlatform = when (Platform.getCurrentPlatform()) {
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
            "abdm-updater-${System.currentTimeMillis()}.$scriptExtension"
        )
        FileSystem.SYSTEM.sink(scriptPathInTempFolder, true)
            .buffer()
            .use {
                it.writeUtf8(scriptContent)
            }
        return scriptPathInTempFolder.toString()
    }

    private fun executeScript() {
        val scriptPath = getScriptPath()
        val logFile = File(logDir, "update_log.txt")
            .apply {
                parentFile.mkdirs()
            }.path

        val command = when (val p = Platform.getCurrentPlatform()) {
            Platform.Desktop.Linux -> execInBash(scriptPath, updateFolder, installationFolder, logFile)
            Platform.Desktop.MacOS -> execInBash(scriptPath, updateFolder, installationFolder, logFile)
            Platform.Desktop.Windows -> execInCMD(scriptPath, updateFolder, installationFolder, logFile)
            else -> error("platform: $p not supported for updating by script")
        }
//        println("execute script $command")
        Runtime.getRuntime().exec(command)
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
            nohop bash "${scriptPath}" "${updateFolder}" "${installationFolder}" > "${logFile}" 2>&1 &
        """.trimIndent()
    }

    override fun installUpdate() {
        executeScript()
    }
}