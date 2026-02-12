package ir.amirab.downloader.exception

// this happens on some CDN (multiple servers/load balancers, serving the same file)
// let's say we ask for 10 connections 7 times they say support resuming and 3 time they say I'm not support resuming.
// if the first initial connection sees that the download doesn't support resuming. so the app shows you it won't support resuming at all.
// if we are detected that the file supports resume we throw this exception
// we shouldn't automatically reset the download, we should retry, or user needs to manually restart the download
class ServerResumeSupportChangeException : DownloadValidationException(
    "Server resume support changed, please restart the download manually"
) {
    override fun isCritical(): Boolean {
        return false
    }
}
