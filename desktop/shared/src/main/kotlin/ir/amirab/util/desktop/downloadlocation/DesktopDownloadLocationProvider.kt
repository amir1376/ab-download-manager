package ir.amirab.util.desktop.downloadlocation

import com.abdownloadmanager.shared.utils.SystemDownloadLocationProvider
import java.io.File

abstract class DesktopDownloadLocationProvider() : SystemDownloadLocationProvider() {
    override fun getCommonDownloadLocation(): File {
        return File(System.getProperty("user.home"), "Downloads")
    }
}