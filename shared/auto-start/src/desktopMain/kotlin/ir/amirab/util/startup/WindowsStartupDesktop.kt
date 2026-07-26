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
    private val hKey = WinReg.HKEY_CURRENT_USER
    private val location = "Software\\Microsoft\\Windows\\CurrentVersion\\Run"

    @Throws(Exception::class)
    override fun install() {
        val data = getExecutableWithArgs()
        Advapi32Util.registrySetStringValue(
            hKey,
            location,
            this.name,
            data
        )
    }

    override fun uninstall() {
        try {
            if (Advapi32Util.registryValueExists(hKey, location, this.name)) {
                Advapi32Util.registryDeleteValue(
                    hKey,
                    location,
                    this.name,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
