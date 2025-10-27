package ir.amirab.downloader.destination

import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.isWindows
import java.io.File

object IncompleteFileUtil {
    private const val SYSTEM_MAXIMUM_FILE_LENGTH = 255
    private const val SYSTEM_MAXIMUM_FULL_PATH_LENGTH = 259
    private fun createExtension(id: Long): String {
        return ".dl-$id.abdm.part"
    }

    fun addIncompleteIndicator(file: File, id: Long): File {
        val ext = createExtension(id)
        if (!file.name.endsWith(ext)) {
            // if the file name is too long, we need to trim it
            // so that the full path length does not exceed the system limit
            // this is a workaround for Windows systems which have a maximum path length of 260 characters
            // see https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file#maximum-path-length-limitation
            val trimmedFileName = if (Platform.isWindows()) {
                // this + 1 is to account for last slash in the path
                val parentPathLength = file.parentFile.path.length + 1
                file.name.take(
                    (SYSTEM_MAXIMUM_FULL_PATH_LENGTH - (parentPathLength + ext.length))
                        // maybe the remaining length is negative which means the file name is too long even after trim!
                        // we hope that filesystem will allow us to create such a file otherwise we will crash!
                        // in that case the user must reduce the path length and try again
                        .coerceAtLeast(0)
                )
            } else {
                // and some other systems which have a maximum file name length of 255 characters
                file.name.take(SYSTEM_MAXIMUM_FILE_LENGTH - ext.length)
            }

            return file.parentFile.resolve(trimmedFileName + ext)
        }
        return file
    }
}
