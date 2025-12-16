package ir.amirab.downloader.connection

interface UserAgentProvider {
    fun getUserAgent(): String?
}
