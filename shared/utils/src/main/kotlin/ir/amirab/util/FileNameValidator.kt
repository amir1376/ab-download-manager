package ir.amirab.util

import java.io.File

object FileNameValidator {
    fun isValidFileName(name: String): Boolean {
        if (name.isEmpty()) return false
        return runCatching {
            File(name).canonicalFile
        }.getOrNull()?.let {
            it.name == name
        } ?: false
    }
}
