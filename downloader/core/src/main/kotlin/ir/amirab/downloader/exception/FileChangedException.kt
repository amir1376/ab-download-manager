package ir.amirab.downloader.exception


sealed class FileChangedException(msg:String, ):DownloadValidationException(msg){
    class LengthChangedException(
        val lastContentLength: Long,
        val newContentLength: Long
    ) : DownloadValidationException(
        "File size changed since last download! last time was $lastContentLength now it's $newContentLength"
    )
    class ETagChangedException(
        val oldETag: String,
        val newETag: String
    ) : DownloadValidationException(
        "File content changed since last download! last time was $oldETag now it's $newETag"
    )
}