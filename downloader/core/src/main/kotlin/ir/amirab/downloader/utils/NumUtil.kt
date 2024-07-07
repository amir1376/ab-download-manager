package ir.amirab.downloader.utils

fun calcPercent(proceed: Long, contentLength: Long): Int {
    return ((proceed.toDouble() / contentLength.toDouble()) * 100).toInt()
}