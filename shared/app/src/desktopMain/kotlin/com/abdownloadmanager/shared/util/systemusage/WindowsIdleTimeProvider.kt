package com.abdownloadmanager.shared.util.systemusage

import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinUser

/**
 * Windows implementation of [DesktopIdleTimeProvider].
 *
 * Uses Win32 `GetLastInputInfo` from `User32.dll` combined with `GetTickCount` from `Kernel32.dll`.
 *
 * Capabilities:
 * - Detects system-wide mouse movement, clicks, and keyboard keystrokes across all windows.
 * - Does not require elevated privileges or accessibility permissions.
 * - Handles 32-bit tick count rollover safely via unsigned arithmetic.
 */
internal class WindowsIdleTimeProvider : DesktopIdleTimeProvider {
    private val lastInputInfo = WinUser.LASTINPUTINFO()

    @Synchronized
    override fun getIdleTimeMillis(): Long? {
        return runCatching {
            if (User32.INSTANCE.GetLastInputInfo(lastInputInfo)) {
                val currentTickCount = Kernel32.INSTANCE.GetTickCount()
                // Unsigned 32-bit arithmetic handles the 49.7 day GetTickCount rollover correctly
                val diff = (currentTickCount - lastInputInfo.dwTime).toLong() and 0xFFFFFFFFL
                diff
            } else {
                null
            }
        }.getOrNull()
    }
}
