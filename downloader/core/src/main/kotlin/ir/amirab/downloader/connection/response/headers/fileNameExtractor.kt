package ir.amirab.downloader.connection.response.headers

import java.nio.charset.Charset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun extractFileNameFromContentDisposition(contentDispositionValue: String): String? {
    utf8FileNameRegex.find(contentDispositionValue)
        ?.groups?.get("fileName")
        ?.value?.let {
            runCatching { FilenameDecoder.decode(it, Charsets.UTF_8) }
                .getOrNull()
        }?.let {
            return it
        }
    asciiFileNameRegex.find(contentDispositionValue)
        ?.groups
        ?.get("fileName")
        ?.value?.let {
            var fileName = it
            fileName = runCatching {
                EmailMimeWordDecoder.decode(fileName)
            }.getOrNull() ?: fileName
            runCatching { FilenameDecoder.decode(fileName, Charsets.UTF_8) }
                .getOrNull()
        }?.let {
            return it
        }
    return null
}

private val asciiFileNameRegex = """filename=(["']?)(?<fileName>.*?[^\\])\1(?:; ?|$)"""
    .toRegex(RegexOption.IGNORE_CASE)

private val utf8FileNameRegex = """filename\*=UTF-8''(?<fileName>[\w%\-\.]+)(?:; ?|${'$'})"""
    .toRegex(RegexOption.IGNORE_CASE)

/**
 * this is very similar to URLDecoder however it doesn't replace "+" with " "
 * RFC 5987
 */
private object FilenameDecoder {
    fun decode(
        encoded: String,
        charset: Charset = Charsets.UTF_8,
    ): String {
        var strIndex = 0
        var byteIndex = 0
        val bytes = ByteArray(encoded.length)
        while (strIndex < encoded.length) {
            val ch = encoded[strIndex]
            if (ch == '%' && (strIndex + 2) < encoded.length) {
                bytes[byteIndex++] = encoded
                    .substring(strIndex + 1, strIndex + 3) // after % take two chars
                    .toInt(16)
                    .toByte()
                strIndex += 3 // %ab (3 chars)
            } else {
                bytes[byteIndex++] = ch.code.toByte()
                strIndex++
            }
        }
        return String(bytes, 0, byteIndex, charset)
    }
}

/**
 * we use this class to decode the filename in content-disposition header in mail servers
 * RFC 2047
 */
private object EmailMimeWordDecoder {
    fun decode(string: String): String {
        return decodeMimeEncodedFilename(string)
    }

    private val regex by lazy {
        """=\?(?<charset>[^?]+)\?(?<encoding>[BQ])\?(?<encodedText>[^?]+)\?="""
            .toRegex(RegexOption.IGNORE_CASE)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeMimeEncodedFilename(input: String): String {
        return regex.replace(input) {
            runCatching {
                val match = it.groups

                val charset = match.requireName("charset").value
                val encoding = match.requireName("encoding").value.uppercase()
                val encodedText = match.requireName("encodedText").value

                val bytes = when (encoding) {
                    "B" -> Base64.decode(encodedText)
                    "Q" -> decodeMimeQuotedPrintable(encodedText)
                    else -> return@replace input
                }
                String(bytes, charset(charset))
            }.getOrNull() ?: it.value
        }
    }

    private fun decodeMimeQuotedPrintable(encoded: String): ByteArray {
        val sb = StringBuilder()

        var i = 0
        while (i < encoded.length) {
            val c = encoded[i]
            when {
                c == '=' && i + 2 < encoded.length -> {
                    val hex = encoded.substring(i + 1, i + 3)
                    val byte = hex.toIntOrNull(16)?.toChar()
                    if (byte != null) {
                        sb.append(byte)
                        i += 3
                    } else {
                        sb.append(c)
                        i++
                    }
                }

                c == '_' -> {
                    sb.append(' ') // _ represents space in Q encoding
                    i++
                }

                else -> {
                    sb.append(c)
                    i++
                }
            }
        }
        return sb.toString().toByteArray(Charsets.ISO_8859_1)
    }

    private fun MatchGroupCollection.requireName(name: String): MatchGroup {
        return requireNotNull(this[name]) {
            "Group $name not found"
        }
    }

}
