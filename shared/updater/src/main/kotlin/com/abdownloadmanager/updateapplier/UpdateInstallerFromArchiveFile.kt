package com.abdownloadmanager.updateapplier

import ir.amirab.util.platform.Platform
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * the duty of the script is
 * it accepts [folderToExtractUpdate], [installationFolder]
 * 1. stop the app
 * 2. remove the installed app files
 * 3. copy [folderToExtractUpdate] into [installationFolder]
 * 4. remove [folderToExtractUpdate]
 * 5. start the app again
 */
class UpdateInstallerFromArchiveFile(
    private val archiveFile: File,
    private val installationFolder: String,
    private val folderToExtractUpdate: File,
    private val appFolderInArchive: String,
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
        extractTo(archiveFile, folderToExtractUpdate)
        val updateFolder = folderToExtractUpdate.resolve(appFolderInArchive)
        require(updateFolder.exists()) {
            "Can't find required files for this update please update it manually"
        }
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
                updateFolder = updateFolder.path,
                installationFolder = installationFolder,
                logFile = logFile,
            )

            Platform.Desktop.MacOS -> execInBash(
                scriptPath = scriptContentFile,
                updateFolder = updateFolder.path,
                installationFolder = installationFolder,
                logFile = logFile,
            )

            Platform.Desktop.Windows -> execInCMD(
                scriptPath = scriptContentFile,
                updateFolder = updateFolder.path,
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

private fun extractTo(archiveFile: File, destinationFolder: File) {
    val name = archiveFile.name
    require(!destinationFolder.isFile) {
        "destination folder is a file!"
    }
    destinationFolder.mkdirs()
    require(destinationFolder.isDirectory) {
        "destination folder is not created!"
    }
    when {
        name.endsWith(".zip") -> extractZip(archiveFile, destinationFolder)
        name.endsWith("tar.gz") -> extractTarGzUsingTar(archiveFile, destinationFolder)
        else -> error("archive file not detected for this file name: $name")
    }
}

private fun extractZip(zipFile: File, outputDirPath: File) {
    ZipInputStream(zipFile.inputStream()).use { zis ->
        var entry: ZipEntry? = null
        while (true) {
            entry = zis.nextEntry
            if (entry == null) break
            val outputFile = outputDirPath.resolve(entry.name)
            if (entry.isDirectory) {
                outputFile.mkdirs()
            } else {
                outputFile.parentFile.mkdirs()
                outputFile.outputStream().use { fileOutputStream ->
                    zis.copyTo(fileOutputStream)
                }
            }
        }
    }
}

private fun extractTarGzUsingTar(tarGzFilePath: File, outputDirPath: File) {
    val tarCommand = listOf("tar", "-xzvf", tarGzFilePath.path, "-C", outputDirPath.path)
    try {
        val process = ProcessBuilder(tarCommand)
            .start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            println("Extraction completed successfully.")
        } else {
            println("Error during extraction. Exit code: $exitCode")
        }
    } catch (e: Exception) {
        println("Failed to execute tar command: ${e.message}")
    }
}
