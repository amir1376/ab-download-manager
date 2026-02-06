package ir.amirab.downloader.connection

object UserAgent {
    const val DEFAULT_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    fun getDefault(): String {
        return DEFAULT_USER_AGENT
    }
}
