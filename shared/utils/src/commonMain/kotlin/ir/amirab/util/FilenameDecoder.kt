package ir.amirab.util

import java.nio.charset.Charset

/**
 * this is very similar to URLDecoder however it doesn't replace "+" with " "
 * RFC 5987
 */
object FilenameDecoder {
    fun decode(
        encoded: String,
        charset: Charset = Charsets.UTF_8,
    ): String {
        var strIndex = 0
        val stringBuilder = StringBuilder()
        // we only initiate it when we visit %
        var bytes: ByteArray? = null
        while (strIndex < encoded.length) {
            var ch = encoded[strIndex]
            if (ch == '%') {
                var byteIndex = 0
                if (bytes == null) {
                    // maximum required size
                    bytes = ByteArray((encoded.length - strIndex) / 3)
                }
                while (true) {
                    if ((strIndex + 2) >= encoded.length) {
                        throw IllegalArgumentException("Incomplete percent encoding at position $strIndex")
                    }
                    bytes[byteIndex++] = Integer.parseInt(
                        encoded,
                        // after % take two chars
                        strIndex + 1,
                        strIndex + 3,
                        16
                    ).toByte()
                    strIndex += 3 // %ab (3 chars)
                    if (strIndex < encoded.length) {
                        ch = encoded[strIndex]
                        if (ch == '%') {
                            continue
                        }
                    }
                    break
                }
                stringBuilder.append(
                    String(bytes, 0, byteIndex, charset)
                )
            } else {
                stringBuilder.append(ch)
                strIndex++
            }
        }
        val modified = bytes != null
        return if (modified) {
            stringBuilder.toString()
        } else {
            encoded
        }
    }
}