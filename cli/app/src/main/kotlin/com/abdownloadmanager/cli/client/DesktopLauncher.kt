package com.abdownloadmanager.cli.client

import com.abdownloadmanager.cli.utils.PortResolver
import java.io.File

/**
 * Launches the desktop AB Download Manager app if it's not already running.
 *
 * Handles platform-specific binary discovery and health-check polling.
 */
object DesktopLauncher {

    private const val MAX_POLL_MS = 30_000L
    private const val POLL_INTERVAL_MS = 500L
    private var desktopProcess: Process? = null

    /**
     * Check if the desktop app is running by pinging its integration server.
     */
    private fun isDesktopRunning(): Boolean {
        val port = PortResolver.readIntegrationPort() ?: return false
        return DesktopClient(port).ping()
    }

    /**
     * Find the desktop app binary on the current platform.
     * Returns null if not found.
     */
    fun findDesktopBinary(): File? {
        val osName = System.getProperty("os.name").lowercase()
        return when {
            osName.contains("win") -> findWindowsBinary()
            osName.contains("mac") || osName.contains("darwin") -> findMacBinary()
            else -> findLinuxBinary()
        }
    }

    /**
     * Launch the desktop app and wait for it to become ready.
     *
     * @return true if the desktop app is ready, false if it couldn't be started or timed out.
     */
    fun ensureDesktopRunning(): Boolean {
        if (isDesktopRunning()) return true

        // If we started the process previously but it died, clean up
        if (desktopProcess != null && !desktopProcess!!.isAlive) {
            desktopProcess = null
        }

        val exe = findDesktopBinary()
        if (exe == null) {
            System.err.println("[abdm] AB Download Manager not found. Please install it first.")
            return false
        }
        System.err.println("[abdm] Starting AB Download Manager...")
        try {
            desktopProcess = ProcessBuilder(exe.absolutePath, "--background")
                .directory(exe.parentFile)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
        } catch (e: Exception) {
            System.err.println("[abdm] Failed to start ABDM: ${e.message}")
            return false
        }
        // Poll /ping until ready or timeout
        val deadline = System.nanoTime() + MAX_POLL_MS * 1_000_000L
        while (System.nanoTime() < deadline) {
            // Fast-fail if the process crashed during startup
            if (desktopProcess != null && !desktopProcess!!.isAlive) {
                System.err.println("[abdm] AB Download Manager process exited prematurely.")
                desktopProcess = null
                return false
            }
            if (isDesktopRunning()) {
                System.err.println("[abdm] AB Download Manager is ready.")
                return true
            }
            try {
                Thread.sleep(POLL_INTERVAL_MS)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return false
            }
        }
        // Timeout — clean up the orphan process
        desktopProcess?.destroyForcibly()
        desktopProcess = null
        System.err.println("[abdm] AB Download Manager did not start within ${MAX_POLL_MS / 1000} seconds.")
        return false
    }

    // --- Platform-specific binary discovery ---

    private fun findWindowsBinary(): File? {
        val localAppData = System.getenv("LOCALAPPDATA")
        val programFiles = System.getenv("ProgramFiles")
        val programFilesX86 = System.getenv("ProgramFiles(x86)")
        val candidates = mutableListOf<File>()
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