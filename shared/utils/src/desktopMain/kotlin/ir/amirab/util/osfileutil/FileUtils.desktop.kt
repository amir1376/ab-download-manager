package ir.amirab.util.osfileutil

import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop

actual fun getPlatformFileUtil(): FileUtils {
    return when (Platform.asDesktop()) {
        Platform.Desktop.Windows -> WindowsFileUtils()
        Platform.Desktop.Linux -> LinuxFileUtils()
        Platform.Desktop.MacOS -> MacOsFileUtils()
    }
}
