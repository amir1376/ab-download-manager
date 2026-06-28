package com.xeton.util.osfileutil

import com.xeton.util.execAndWait
import java.io.File

internal class MacOsFileUtils : DesktopFileUtils() {
    override fun openFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("open", file.path))
    }

    override fun openFolderOfFileInternal(file: File): Boolean {
        return execAndWait(arrayOf("open", "-R", file.path))
    }

    override fun openFolderInternal(folder: File): Boolean {
        return execAndWait(arrayOf("open", folder.path))
    }

}
