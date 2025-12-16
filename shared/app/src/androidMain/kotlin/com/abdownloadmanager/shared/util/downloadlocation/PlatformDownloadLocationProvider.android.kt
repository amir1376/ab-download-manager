package com.abdownloadmanager.shared.util.downloadlocation

import android.os.Environment
import com.abdownloadmanager.shared.util.SystemDownloadLocationProvider
import java.io.File

class AndroidDownloadLocationProvider : SystemDownloadLocationProvider() {
    override fun getCommonDownloadLocation(): File {
        return Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .absoluteFile
    }

    override fun getCurrentDownloadLocation(): File {
        return getCommonDownloadLocation()
    }

}

actual fun getPlatformDownloadLocationProvider(): SystemDownloadLocationProvider {
    return AndroidDownloadLocationProvider()
}
