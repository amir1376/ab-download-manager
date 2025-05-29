package ir.amirab.downloader.exception

abstract class DownloadValidationException(
    msg: String,
    cause: Throwable? = null,
) : Exception(msg, cause) {
    abstract fun isCritical(): Boolean
}
