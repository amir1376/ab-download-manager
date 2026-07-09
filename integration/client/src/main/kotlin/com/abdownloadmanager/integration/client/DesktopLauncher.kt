package com.abdownloadmanager.integration.client

import java.io.File

/**
 * Launches the desktop AB Download Manager app if it's not already running.
 *
 * Binary discovery is delegated to [binaryProvider], allowing different
 * environments (CLI / desktop tests) to supply their own discovery strategy.
 *
 * @param binaryProvider A function that returns the path to the desktop app
 *                       binary, or null if not found.
 */
class DesktopLauncher(
    private val binaryProvider: () -> File?,
) {
    companion object {
        private const val MAX_POLL_MS = 30_000L
        private const val POLL_INTERVAL_MS = 500L
    }

    private var desktopProcess: Process? = null

    /**
     * Check if the desktop app is running by pinging its integration server.
     */
    private fun isDesktopRunning(): Boolean {
        val port = PortResolver.readIntegrationPort() ?: return false
        return DesktopClient(port).ping()
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

        val exe = binaryProvider()
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
}