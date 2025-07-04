package ir.amirab.util

import ir.amirab.util.osfileutil.FileUtils
import java.io.File

object PathValidator {
    fun canWriteToThisPath(path: String): Boolean {
        return FileUtils.canWriteInThisFolder(path)
    }

    fun isValidPath(path: String): Boolean {
        if (path.isEmpty()) return false
        return runCatching {
            File(path).canonicalFile
            true
        }.getOrElse { false }
    }
}
