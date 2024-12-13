package com.abdownloadmanager.updateapplier;

import com.abdownloadmanager.updatechecker.UpdateInfo
import okio.use
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class DesktopUpdateApplier(
    private val installationFolder: String?,
    private val updateFolder: String,
    private val logDir: String,
    private val updateDownloader: UpdateDownloader,
) : UpdateApplier {
    private var downloading: Boolean = false
    override fun updateSupported(): Boolean {
        val installationFolder = installationFolder ?: return false
        return File(installationFolder).canWrite()
    }

    override suspend fun applyUpdate(
        updateInfo: UpdateInfo,
    ) {
        if (!updateSupported()) {
            return
        }
        val installationFolder = requireNotNull(installationFolder) {
            "update applier can only apply update if installation folder is not null"
        }
        //it is only check for same instance
        // if I faced to multiple update (when user press "update" many times)
        // I have to cancel this suspension job and create a new instance instead
        if (downloading) {
            return
        }
        downloading = true

        val downloadedFile = updateDownloader.downloadUpdate(updateInfo)
        val updateFilesDirectory = File(updateFolder)
        if (!downloadedFile.exists()) {
            downloading = false
            return
        }
        extractTo(downloadedFile, updateFilesDirectory)
//        updateDownloader.removeUpdate(updateInfo)
        val updateFolder = updateFilesDirectory
            .resolve("ABDownloadManager")
        require(updateFolder.exists()) {
            "Can't find required files for this update please update it manually"
        }
        UpdateInstallerByScript(
            installationFolder = installationFolder,
            updateFolder = updateFolder.path,
            logDir = logDir
        ).installUpdate()
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
