package ir.amirab.util.startup

import java.io.File

class UnixXDGStartupDesktop(
    name: String,
    path: String,
    args: List<String>,
    val desktopEntryFileName: String,
) : AbstractDesktopStartupManager(
    name = name,
    path = path,
    args = args,
) {

    private fun getIconFilePath(): String? {
        return runCatching {
            val file = File(path)
            val name = file.name
            return file
                .parentFile.parentFile
                .resolve("lib/$name.png")
                .takeIf { it.exists() }?.path
        }.getOrNull()
    }

    private fun getAutoStartFile(): File {
        if (!autostartDir.exists()) {
            autostartDir.mkdirs()
        }
        return File(autostartDir, "$desktopEntryFileName.desktop")
    }

    private fun getSystemdServiceFile(): File {
        if (!systemdUserDir.exists()) {
            systemdUserDir.mkdirs()
        }
        return File(systemdUserDir, "$desktopEntryFileName.service")
    }

    @Throws(Exception::class)
    override fun install() {
        val name = this.name
        val exec = getExecutableWithArgs()
        val icon = getIconFilePath()
        
        // Create XDG autostart desktop file (for traditional desktop environments)
        getAutoStartFile().writeText(
            buildString {
                appendLine("[Desktop Entry]")
                appendLine("Type=Application")
                appendLine("Name=$name")
                appendLine("Exec=$exec")
                icon?.let { icon ->
                    appendLine("Icon=$icon")
                }
                appendLine("Terminal=false")
                appendLine("NoDisplay=true")
            }
        )
        
        // Create systemd user service (for window managers like Hyprland, i3, sway, etc.)
        installSystemdService(name, exec)
    }

    private fun installSystemdService(name: String, exec: String) {
        try {
            getSystemdServiceFile().writeText(
                buildString {
                    appendLine("[Unit]")
                    appendLine("Description=$name")
                    appendLine("After=default.target")
                    appendLine()
                    appendLine("[Service]")
                    appendLine("Type=simple")
                    appendLine("ExecStart=$exec")
                    appendLine("Restart=on-failure")
                    appendLine("RestartSec=5")
                    appendLine()
                    appendLine("[Install]")
                    appendLine("WantedBy=default.target")
                }
            )
            
            // Enable the service
            runCatching {
                Runtime.getRuntime().exec(
                    arrayOf("systemctl", "--user", "enable", "$desktopEntryFileName.service")
                ).waitFor()
            }
        } catch (e: Exception) {
            // If systemd service creation fails, it's not critical
            // The XDG desktop file should still work for traditional DEs
            println("Warning: Failed to create systemd service for autostart: ${e.message}")
        }
    }

    override fun uninstall() {
        // Remove XDG autostart desktop file
        getAutoStartFile().delete()
        
        // Remove systemd service
        uninstallSystemdService()
    }

    private fun uninstallSystemdService() {
        try {
            // Disable the service first
            runCatching {
                Runtime.getRuntime().exec(
                    arrayOf("systemctl", "--user", "disable", "$desktopEntryFileName.service")
                ).waitFor()
            }
            
            // Remove the service file
            getSystemdServiceFile().delete()
            
            // Reload systemd daemon
            runCatching {
                Runtime.getRuntime().exec(
                    arrayOf("systemctl", "--user", "daemon-reload")
                ).waitFor()
            }
        } catch (e: Exception) {
            // If systemd service removal fails, it's not critical
            println("Warning: Failed to remove systemd service: ${e.message}")
        }
    }

    companion object {
        val autostartDir: File
            get() {
                val home = System.getProperty("user.home")
                return File("$home/.config/autostart/")
            }
        
        val systemdUserDir: File
            get() {
                val home = System.getProperty("user.home")
                return File("$home/.config/systemd/user/")
            }
    }
}
