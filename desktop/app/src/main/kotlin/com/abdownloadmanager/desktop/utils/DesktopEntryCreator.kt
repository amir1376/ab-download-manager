package com.abdownloadmanager.desktop.utils

import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isLinux
import java.io.File

object DesktopEntryCreator {
    fun createLinuxDesktopEntry() {
        if (!Platform.isLinux()) {
            return
        }
        runCatching {
            LinuxDesktopEntryCreator.createDesktopEntry(
                name = AppInfo.displayName,
                comment = "Manage and organize your download files better than before",
                desktopEntryFilename = "abdownloadmanager",
                execFile = requireNotNull(AppInfo.exeFile) {
                    "Exe file not known"
                },
                startupWMClass = "com-abdownloadmanager-desktop-AppKt"
            )
        }.onFailure {
            it.printStackTrace()
        }
    }
}


private object LinuxDesktopEntryCreator {
    fun createDesktopEntry(
        name: String,
        execFile: String,
        comment: String,
        desktopEntryFilename: String,
        startupWMClass: String,
    ) {
        val iconFilePath = requireNotNull(getIconFilePath(execFile)) {
            "Icon path is null! for this exe file: $execFile"
        }
        val desktopEntryContent = buildString {
            appendLine("[Desktop Entry]")
            appendLine("Name=$name")
            appendLine("Comment=$comment")
            appendLine("GenericName=Downloader")
            appendLine("Categories=Utility;Network;")
            appendLine("Exec=$execFile")
            appendLine("Icon=$iconFilePath")
            appendLine("Terminal=false")
            appendLine("Type=Application")
            appendLine("StartupWMClass=${startupWMClass}")
        }
        val homePath = System.getProperty("user.home")
        val desktopEntryFile = File(homePath, "/.local/share/applications/${desktopEntryFilename}.desktop")
        desktopEntryFile.writeText(desktopEntryContent)
    }

    private fun getIconFilePath(execFile: String): String? {
        return runCatching {
            val file = File(execFile)
            val name = file.name
            return file
                .parentFile.parentFile
                .resolve("lib/$name.png")
                .takeIf { it.exists() }?.path
        }.getOrNull()
    }
}
