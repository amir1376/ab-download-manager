package ir.amirab.util

import java.io.File


fun File.toUpUntil(
    condition: (File) -> Boolean
): File? {
    var file: File? = this
    while (true) {
        if (file == null) {
            return null
        }
        if (condition(file)) {
            return file
        }
        file = file.parentFile
    }
}
