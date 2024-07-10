package ir.amirab.downloader.exception

class NoSpaceInStorageException(
    val available: Long,
    val required: Long
) : DownloadValidationException(
    "No space available required=$required , available=$available"
) {
    override fun isCritical(): Boolean {
        // there is no space in users file system so we should stop
        return true
    }
}
