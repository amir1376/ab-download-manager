package ir.amirab.downloader.part

fun interface MinPartSizeProvider {
    // will be called in hot loop
    // it should be non-blocking and return fast
    fun getMinPartSize(): Long
}
