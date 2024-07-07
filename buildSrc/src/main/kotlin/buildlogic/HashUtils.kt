package buildlogic

import java.io.File
import java.security.MessageDigest

// I should move these classes/objects somewhere organized
object HashUtils {
    private fun ByteArray.toHexString(): String = joinToString("", transform = { "%02x".format(it) })
    fun md5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(file.readBytes())
        return digest.toHexString()
    }
}