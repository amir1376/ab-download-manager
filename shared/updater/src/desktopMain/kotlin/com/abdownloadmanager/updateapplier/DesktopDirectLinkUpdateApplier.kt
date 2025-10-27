package com.abdownloadmanager.updateapplier

import com.abdownloadmanager.updatechecker.UpdateInfo
import com.abdownloadmanager.updatechecker.UpdateSource
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isMac
import java.io.File

class DesktopDirectLinkUpdateApplier(
    private val installationFolder: String?,
    private val appName: String,
    private val updateFolder: String,
    private val logDir: String,
    private val updatePreparer: UpdateDownloader,
) : BaseUpdateApplier() {
    override fun getUpdatePreparer(): UpdatePreparer {
        return updatePreparer
    }
    override fun updateSupported(): Boolean {
        val installationFolder = installationFolder ?: return false
        return File(installationFolder).canWrite()
    }

    override fun validateAppStateOnApplyUpdate() {
        requireNotNull(installationFolder) {
            "update applier can only apply update if installation folder is not null"
        }
    }

    override fun getBestDownloadSource(updateInfo: UpdateInfo): UpdateSource {
        val downloadableSources =
            updateInfo.updateSource.filterIsInstance<UpdateSource.DirectDownloadLink>()
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
        return requireNotNull(downloadSource) {
            "Can't find proper download link for your platform! Please update it manually"
        }
    }

    override fun getUpdateInstaller(preparedUpdate: UpdatePreparer.PreparedUpdate): UpdateInstaller {
        requireNotNull(preparedUpdate is UpdateDownloader.PreparedUpdateFile)
        val downloadedFile = (preparedUpdate as UpdateDownloader.PreparedUpdateFile).file
        return when {
            isArchiveFile(downloadedFile.name) -> {
                val appFolderInArchive = when {
                    Platform.isMac() -> "$appName.app"
                    else -> appName
                }
                UpdateInstallerFromArchiveFile(
                    archiveFile = downloadedFile,
                    installationFolder = installationFolder!!, // validated
                    appFolderInArchive = appFolderInArchive,
                    folderToExtractUpdate = File(updateFolder).resolve("extracted"),
                    logDir = logDir,
                )
            }

            isExeFile(downloadedFile.name) -> {
                UpdateInstallerByWindowsExecutable(downloadedFile)
            }

            else -> {
                // should not happen btw
                error("can't install ${extension(downloadedFile.name)} format automatically! please update it manually!")
            }
        }
    }

    private fun isAppInstalledWithNSIS(): Boolean {
        return File(installationFolder, "uninstall.exe").exists()
    }


    private fun isArchiveFile(name: String): Boolean {
        return name.endsWith(".tar.gz") || name.endsWith(".zip")
    }

    private fun isExeFile(name: String): Boolean {
        return name.endsWith(".exe")
    }
}
