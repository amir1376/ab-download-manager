package com.abdownloadmanager.desktop.utils

import ir.amirab.downloader.utils.ByteConverter

data class HumanReadableSize(
    val value:Double,
    val unit:Long,
)

fun baseConvertBytesToHumanReadable(size: Long):HumanReadableSize?{
    return ByteConverter.run {
        when (size) {
            in Long.MIN_VALUE until 0 -> null

            in 0 until K_BYTES -> {
                HumanReadableSize(
                    size.toDouble(),
                    BYTES,
                )
            }

            in K_BYTES until M_BYTES -> {
                HumanReadableSize(
                    byteTo(size, K_BYTES),
                    K_BYTES,
                )
            }

            in M_BYTES until G_BYTES -> {
                HumanReadableSize(
                    byteTo(size, M_BYTES),
                    M_BYTES
                )
            }

            in G_BYTES..Long.MAX_VALUE -> {
                HumanReadableSize(
                    byteTo(size, G_BYTES),
                    G_BYTES,
                )
            }

            else -> error("should not happened! we covered all range but not this ? $size")
        }
    }
}
fun convertBytesToHumanReadable(size: Long): String? {
    ByteConverter.run {
        return baseConvertBytesToHumanReadable(size)?.let {
            "${prettify(it.value)} ${unitPrettify(it.unit)}"
        }
    }
}

fun convertSizeToHumanReadable(size: Long): String {
    return convertBytesToHumanReadable(size) ?: "unknown"
}

fun convertSpeedToHumanReadable(size: Long, perUnit: String="s"): String {
    return convertBytesToHumanReadable(size)?.let {
        "$it/$perUnit"
    } ?: "-"
}
//fun main() {
//    println(convertBytesToHumanReadable(2048000))
//}
