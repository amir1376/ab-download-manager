package ir.amirab.downloader.exception

class PrepareDestinationFailedException(
    e: Exception
) : DownloadValidationException(
    "Problem in preparing output: ${e.localizedMessage}",
    e,
) {
    override fun isCritical(): Boolean {
        // there is a problem when preparing destination. retry doesn't work here
        return true
    }
}
