package com.abdownloadmanager.shared.util.downloadlocation

import com.sun.jna.platform.win32.KnownFolders
import com.sun.jna.platform.win32.Shell32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.PointerByReference
import java.io.File

class WindowsDownloadLocationProvider : DesktopDownloadLocationProvider() {
    override fun getCurrentDownloadLocation(): File? {
        val pathRef = PointerByReference()
        val hr = Shell32.INSTANCE.SHGetKnownFolderPath(KnownFolders.FOLDERID_Downloads, 0, WinNT.HANDLE(), pathRef)
        if (hr.toInt() != 0) {
            throw RuntimeException("Failed to get Downloads folder (HRESULT=${hr.toInt()})")
        }
        val downloadsPath = pathRef.value.getWideString(0)
        return File(downloadsPath).canonicalFile
    }
}
