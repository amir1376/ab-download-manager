package com.abdownloadmanager.shared.util.systemusage

/**
 * Platform-agnostic contract for querying system-wide idle time on desktop platforms.
 */
internal interface DesktopIdleTimeProvider {
    /**
     * Returns the system-wide idle time in milliseconds since the last detected user input
     * (keyboard, mouse, or touch), or `null` if the native API failed or is unsupported on this system.
     */
    fun getIdleTimeMillis(): Long?
}
