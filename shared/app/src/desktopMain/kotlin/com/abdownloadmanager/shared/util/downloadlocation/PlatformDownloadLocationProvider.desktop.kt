package com.abdownloadmanager.shared.util.downloadlocation

import com.abdownloadmanager.shared.util.SystemDownloadLocationProvider
import com.xeton.util.platform.Platform
import com.xeton.util.platform.asDesktop

actual fun getPlatformDownloadLocationProvider(): SystemDownloadLocationProvider {
    return when (Platform.asDesktop()) {
        Platform.Desktop.Windows -> WindowsDownloadLocationProvider()
        Platform.Desktop.Linux -> LinuxDownloadLocationProvider()
        Platform.Desktop.MacOS -> MacDownloadLocationProvider()
    }
}
