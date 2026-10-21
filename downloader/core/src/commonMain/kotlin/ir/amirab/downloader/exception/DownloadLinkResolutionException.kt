package ir.amirab.downloader.exception

class DownloadLinkResolutionException(message: String) : DownloadValidationException(message) {
    override fun isCritical(): Boolean = true
}
