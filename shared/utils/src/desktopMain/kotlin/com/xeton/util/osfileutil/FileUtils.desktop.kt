package com.xeton.util.osfileutil

import com.xeton.util.platform.Platform
import com.xeton.util.platform.asDesktop

actual fun getPlatformFileUtil(): FileUtils {
    return when (Platform.asDesktop()) {
        Platform.Desktop.Windows -> WindowsFileUtils()
        Platform.Desktop.Linux -> LinuxFileUtils()
        Platform.Desktop.MacOS -> MacOsFileUtils()
    }
}
