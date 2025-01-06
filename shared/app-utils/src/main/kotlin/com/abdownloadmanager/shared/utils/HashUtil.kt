package com.abdownloadmanager.shared.utils

import ir.amirab.downloader.utils.calcPercent
import java.io.File
import java.io.InputStream
import java.security.MessageDigest

sealed class FileChecksumAlgorithm(
    val algorithm: String,
) {
    data object MD5 : FileChecksumAlgorithm("MD5")
    data object SHA1 : FileChecksumAlgorithm("SHA-1")
    data object SHA256 : FileChecksumAlgorithm("SHA-256")
    data object SHA512 : FileChecksumAlgorithm("SHA-512")

    companion object {
        fun default() = SHA256
        fun all() = listOf(
            MD5,
            SHA1,
            SHA256,
            SHA512,
        )
    }
}

data class FileChecksum(
    val algorithm: String,
    val value: String,
) {

    override fun toString(): String {
        return "$algorithm:$value"
    }

    companion object {
        fun fromString(string: String): FileChecksum {
            val segments = string.split(":")
            require(segments.size == 2) {
                "Invalid checksum string: $string it should be in format algorithm:value"
            }
            return FileChecksum(
                algorithm = segments[0],
                value = segments[1],
            )
        }

        fun fromNullableString(string: String?): FileChecksum? {
            return string?.let {
                fromString(it)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileChecksum
        return algorithm.equals(other.algorithm, true) && value.equals(other.value, true)
    }

    override fun hashCode(): Int {
        var result = algorithm.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

object HashUtil {
    fun hash(
        algorithm: String,
        inputStream: InputStream,
        size: Long,
        onNewPercent: (Int) -> Unit,
    ): String {
        val messageDigest = MessageDigest.getInstance(algorithm)
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var processedBytes = 0L
        var lastPercent = 0
        while (true) {
            val readCount = inputStream.read(buffer)
            if (readCount == -1) {
                break
            }
            messageDigest.update(buffer, 0, readCount)
            processedBytes += readCount
            val newPercent = calcPercent(processedBytes, size)
            if (newPercent != lastPercent) {
                onNewPercent(newPercent)
                lastPercent = newPercent
            }
        }
        return messageDigest
            .digest()
            .joinToString("") {
                "%02x".format(it)
            }
    }

    fun fileHash(
        algorithm: String,
        file: File,
        onNewPercent: (Int) -> Unit
    ): String {
        val fileSize = file.length()
        return file.inputStream().use {
            hash(
                algorithm = algorithm,
                inputStream = it,
                size = fileSize,
                onNewPercent = onNewPercent
            )
        }
    }
}
