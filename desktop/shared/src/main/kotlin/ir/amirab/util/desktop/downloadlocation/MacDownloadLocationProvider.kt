package ir.amirab.util.desktop.downloadlocation

import java.io.File

class MacDownloadLocationProvider : DesktopDownloadLocationProvider() {
    override fun getCurrentDownloadLocation(): File? {
        return getCommonDownloadLocation()
    }
}