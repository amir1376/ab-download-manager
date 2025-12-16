package ir.amirab.downloader.connection

interface IResponseInfo {
    val isSuccessFul: Boolean
    val requiresAuth: Boolean
    val requireBasicAuth: Boolean
    val resumeSupport: Boolean
}
