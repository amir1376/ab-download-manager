package ir.amirab.downloader.exception


sealed class FileChangedException(msg: String) : DownloadValidationException(msg) {
    override fun isCritical(): Boolean {
        // download must stop immediately
        return true
    }

    class LengthChangedException(
        val lastContentLength: Long,
        val newContentLength: Long
    ) : FileChangedException(
        "File size changed since last download! last time was $lastContentLength now it's $newContentLength"
    )

    class ETagChangedException(
        val oldETag: String,
        val newETag: String
    ) : FileChangedException(
        "File content changed since last download! last time was $oldETag now it's $newETag"
    )

    class GotAWebPage : FileChangedException(
        "link is a webpage"
    )
}