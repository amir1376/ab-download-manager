package ir.amirab.util.osfileutil

import ir.amirab.util.platform.Platform
import okio.Path
import java.io.File

interface FileUtils {
    fun openFile(file: File): Boolean
    fun openFolderOfFile(file: File): Boolean
    fun openFolder(folder: File): Boolean
    fun canWriteInThisFolder(folder: String): Boolean
    fun isRemovableStorage(path: String): Boolean
    
    companion object : FileUtils by getPlatformFileUtil()
}

private fun getPlatformFileUtil(): FileUtils {
    return when (Platform.getCurrentPlatform()) {
        Platform.Desktop.Windows -> WindowsFileUtils()
        Platform.Desktop.Linux -> LinuxFileUtils()
        Platform.Desktop.MacOS -> MacOsFileUtils()
        Platform.Android -> JVMFileUtils()
    }
}
