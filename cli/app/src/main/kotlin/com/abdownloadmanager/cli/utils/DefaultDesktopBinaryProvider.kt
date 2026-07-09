package com.abdownloadmanager.cli.utils

import java.io.File

/**
 * Default binary provider for CLI: discovers the desktop AB Download Manager
 * app binary.
 *
 * Resolution order:
 * 1. jpackage.app-path system property (used by the desktop app's AppInfo.exeFile)
 * 2. Standard platform-specific install paths (fallback for CLI, which is not
 *    launched via jpackage)
 *
 * No registry reading — per maintainer directive.
 */
object DefaultDesktopBinaryProvider : () -> File? {

    override operator fun invoke(): File? {
        // First try: jpackage app-path (same mechanism as AppInfo.exeFile)
        val jpackagePath = System.getProperty("jpackage.app-path")
        if (jpackagePath != null) {
            return File(jpackagePath)
        }

        // Fallback: standard platform-specific paths
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("win") -> findWindowsBinary()
            osName.contains("mac") || osName.contains("darwin") -> findMacBinary()
            else -> findLinuxBinary()
        }
    }

    private fun findWindowsBinary(): File? {
        val candidates = mutableListOf<File>()

        // Standard install locations
        val localAppData = System.getenv("LOCALAPPDATA")
        val programFiles = System.getenv("ProgramFiles")
        val programFilesX86 = System.getenv("ProgramFiles(x86)")
        if (localAppData != null) {
            candidates.add(File(localAppData, "ABDownloadManager/ABDownloadManager.exe"))
            candidates.add(File(localAppData, "AB Download Manager/ABDownloadManager.exe"))
        }
        if (programFiles != null) {
            candidates.add(File(programFiles, "AB Download Manager/ABDownloadManager.exe"))
        }
        if (programFilesX86 != null) {
            candidates.add(File(programFilesX86, "AB Download Manager/ABDownloadManager.exe"))
        }

        // User home fallback
        candidates.add(File(System.getProperty("user.home"), "ABDownloadManager/ABDownloadManager.exe"))

        return candidates.firstOrNull { it.exists() }
    }

    private fun findMacBinary(): File? {
        val candidates = listOf(
            File("/Applications/AB Download Manager.app/Contents/MacOS/AB Download Manager"),
            File(System.getProperty("user.home"), "Applications/AB Download Manager.app/Contents/MacOS/AB Download Manager"),
        )
        return candidates.firstOrNull { it.exists() }
    }

    private fun findLinuxBinary(): File? {
        val candidates = listOf(
            File(System.getProperty("user.home"), ".local/bin/ABDownloadManager"),
            File("/usr/local/bin/ABDownloadManager"),
            File("/usr/bin/ABDownloadManager"),
        )
        return candidates.firstOrNull { it.exists() }
    }
}