package ir.amirab.downloader

data class DownloadSettings(
    //can be changed after boot!
    var defaultThreadCount: Int = DEFAULT_THREAD_COUNT,
    var dynamicPartCreationMode: Boolean = true,
    var useServerLastModifiedTime: Boolean = false,
    var globalSpeedLimit: Long = 0,//unlimited
    var useSparseFileAllocation: Boolean = true,
    var minPartSize: Long = DEFAULT_MIN_PART_SIZE,
    var maxDownloadRetryCount: Int = 0,
    // WARNING: this is used in boot so make sure to update it before booting
    // make it val or add a way to reload it properly
    var appendExtensionToIncompleteDownloads: Boolean = false,
) {
    companion object {
        const val DEFAULT_THREAD_COUNT: Int = 8
        const val DEFAULT_MIN_PART_SIZE: Long = 512 * 1024L
        const val MIN_ALLOWED_MIN_PART_SIZE: Long = 1 * 1024L
        const val MAX_ALLOWED_MIN_PART_SIZE: Long = 2 * 1024 * 1024 * 1024L
    }
}
