package ir.amirab.downloader.exception

class TooManyErrorException(
    override val cause: Throwable
) : Exception(
    "Download is stopped because all parts exceeds max retries",
) {
    fun findActualDownloadErrorCause(): Throwable {
        return when (cause) {
            is PartTooManyErrorException -> cause.cause
            else -> cause
        }
    }
}
