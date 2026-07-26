package ir.amirab.util.startup

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg

class WindowsStartupDesktop(
    name: String,
    path: String,
    args: List<String>,
) : AbstractDesktopStartupManager(
    name = name,
    path = path,
    args = args
) {
    @Throws(Exception::class)
    override fun install() {
        val data = getExecutableWithArgs()
        Advapi32Util.registrySetStringValue(
            WinReg.HKEY_CURRENT_USER,
            "Software\\Microsoft\\Windows\\CurrentVersion\\Run",
            this.name,
            data
        )
    }

    override fun uninstall() {
        try {
            val location = "Software\\Microsoft\\Windows\\CurrentVersion\\Run"
            if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, location, this.name)) {
                Advapi32Util.registryDeleteValue(
                    WinReg.HKEY_CURRENT_USER,
                    location,
                    this.name,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
