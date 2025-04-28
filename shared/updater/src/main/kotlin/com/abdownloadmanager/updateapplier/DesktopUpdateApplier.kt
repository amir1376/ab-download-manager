package com.abdownloadmanager.updateapplier;

import com.abdownloadmanager.updatechecker.UpdateInfo
import com.abdownloadmanager.updatechecker.UpdateSource
import ir.amirab.util.platform.Platform
import java.io.File

class DesktopUpdateApplier(
    private val installationFolder: String?,
    private val appName: String,
    private val updateFolder: String,
    private val logDir: String,
    private val updateDownloader: UpdateDownloader,
) : UpdateApplier {
    private var downloading: Boolean = false
    override fun updateSupported(): Boolean {
        val installationFolder = installationFolder ?: return false
        return File(installationFolder).canWrite()
    }

    private fun isAppInstalledWithNSIS(): Boolean {
        return File(installationFolder, "uninstall.exe").exists()
    }

    private fun extension(name: String): String {
        return name.substringAfterLast('.', "")
    }

    private fun isArchiveFile(name: String): Boolean {
        return name.endsWith(".tar.gz") || name.endsWith(".zip")
    }

    private fun isExeFile(name: String): Boolean {
        return name.endsWith(".exe") || name.endsWith(".zip")
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
        val downloadableSources = updateInfo.updateSource.filterIsInstance<UpdateSource.DirectDownloadLink>()
        var downloadSource = downloadableSources.find {
            isArchiveFile(it.name)
        }
        if (Platform.getCurrentPlatform() == Platform.Desktop.Windows) {
            val exeDirectDownloadLink = downloadableSources.find {
                isExeFile(it.name)
            }
            if (isAppInstalledWithNSIS() && exeDirectDownloadLink != null) {
                downloadSource = exeDirectDownloadLink
            }
        }
        requireNotNull(downloadSource) {
            "Can't find proper download link for your platform! Please update it manually"
        }
        val downloadedFile = try {
            updateDownloader.downloadUpdate(downloadSource)
        } catch (e:Exception) {
            downloading = false
            throw  e
        }
        if (!downloadedFile.exists()) {
            downloading = false
            return
        }
        val updateInstaller = when {
            isArchiveFile(downloadSource.name) -> {
                UpdateInstallerFromArchiveFile(
                    archiveFile = downloadedFile,
                    installationFolder = installationFolder,
                    appFolderInArchive = appName,
                    folderToExtractUpdate = File(updateFolder).resolve("extracted"),
                    logDir = logDir,
                )
            }

            isExeFile(downloadedFile.name) -> {
                UpdateInstallerByWindowsExecutable(downloadedFile)
            }

            else -> {
                // should not happen btw
                error("can't install ${extension(downloadSource.name)} format automatically! please update it manually!")
            }
        }
//        updateDownloader.removeUpdate(updateInfo)
        try {
            updateInstaller.installUpdate()
        } catch (e: Exception) {
            throw RuntimeException(
                buildString {
                    appendLine("can't start installation")
                    e.localizedMessage?.let(this::append)
                },
                e,
            )
        }
    }
    override suspend fun cleanup() {
        updateDownloader.removeAllUpdates()
    }
}
