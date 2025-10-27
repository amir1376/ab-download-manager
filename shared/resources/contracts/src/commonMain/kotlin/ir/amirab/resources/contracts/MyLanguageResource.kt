package ir.amirab.resources.contracts

import okio.FileSystem
import okio.Path

sealed interface MyLanguageResource {
    val language: String
    val getData: suspend () -> ByteArray

    data class BundledLanguageResource(
        override val language: String,
        override val getData: suspend () -> ByteArray,
    ) : MyLanguageResource

    class ExternalLanguageResource(
        val path: Path,
    ) : MyLanguageResource {
        override val language: String
            get() = path.name.substringBeforeLast(".")
        override val getData: suspend () -> ByteArray = {
            FileSystem.SYSTEM.read(path) {
                readByteArray()
            }
        }

    }
}
