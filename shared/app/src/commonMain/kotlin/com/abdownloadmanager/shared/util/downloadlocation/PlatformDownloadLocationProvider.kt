package com.abdownloadmanager.shared.util.downloadlocation

import com.abdownloadmanager.shared.util.SystemDownloadLocationProvider

object PlatformDownloadLocationProvider {
    val instance: SystemDownloadLocationProvider by lazy {
        getPlatformDownloadLocationProvider()
    }
}

expect fun getPlatformDownloadLocationProvider(): SystemDownloadLocationProvider

