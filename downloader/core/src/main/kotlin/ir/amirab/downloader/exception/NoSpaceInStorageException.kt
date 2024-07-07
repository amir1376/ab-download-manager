package ir.amirab.downloader.exception

class NoSpaceInStorageException(
    val available: Long,
    val required: Long
) : DownloadValidationException(
    "No space available required=$required , available=$available"
)
