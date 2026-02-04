package org.jetbrains.jewel.window.utils.linux

/**
 * Simple detection of the current Linux Desktop Environment.
 * Provides a minimal API expected by LinuxTitleBarIconsFactory.
 */
public enum class LinuxDesktopEnvironment {
    GNOME,
    KDE,
    UNKNOWN,
    ;

    public companion object {
        /**
         * Detect the current desktop environment once and expose as a constant-like property.
         */
        public val Current: LinuxDesktopEnvironment by lazy { detectCurrent() }

        private fun detectCurrent(): LinuxDesktopEnvironment {
            // Only try to detect if running on a Linux-like OS. Otherwise default to UNKNOWN.
            val osName = System.getProperty("os.name")?.lowercase() ?: return UNKNOWN
            if (!osName.contains("linux")) return UNKNOWN

            // Check common environment variables
            val env = System.getenv()
            val xdgDesktop = env["XDG_CURRENT_DESKTOP"]?.lowercase().orEmpty()
            val desktopSession = env["DESKTOP_SESSION"]?.lowercase().orEmpty()
            val kdeFull = env["KDE_FULL_SESSION"]?.lowercase().orEmpty()
            val gnomeSession = env["GNOME_DESKTOP_SESSION_ID"]?.lowercase().orEmpty()
            val waylandSession = env["XDG_SESSION_DESKTOP"]?.lowercase().orEmpty()

            // KDE heuristics
            if (
                xdgDesktop.contains("kde") ||
                desktopSession.contains("kde") ||
                kdeFull == "true" ||
                waylandSession.contains("kde")
            ) {
                return KDE
            }

            // GNOME heuristics
            if (
                xdgDesktop.contains("gnome") ||
                desktopSession.contains("gnome") ||
                gnomeSession.isNotEmpty() ||
                waylandSession.contains("gnome")
            ) {
                return GNOME
            }

            // Some environments expose combined values like "X-Cinnamon:GNOME" etc.
            if (":" in xdgDesktop) {
                val parts = xdgDesktop.split(":").map { it.trim() }
                if (parts.any { it.contains("kde") }) return KDE
                if (parts.any { it.contains("gnome") }) return GNOME
            }

            return UNKNOWN
        }
    }
}
