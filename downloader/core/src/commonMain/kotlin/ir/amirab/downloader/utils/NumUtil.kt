package ir.amirab.downloader.utils

fun calcPercent(proceed: Long, contentLength: Long): Int {
    return ((proceed.toDouble() / contentLength.toDouble()) * 100).toInt()
}

fun calcPercent(proceed: Int, contentLength: Int): Int {
    return ((proceed.toDouble() / contentLength.toDouble()) * 100).toInt()
}

fun calcPercent(proceed: Double, contentLength: Double): Int {
    return ((proceed / contentLength) * 100).toInt()
}
