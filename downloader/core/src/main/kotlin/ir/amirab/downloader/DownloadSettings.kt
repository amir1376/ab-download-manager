package ir.amirab.downloader

data class DownloadSettings(
    //can be changed after boot!
    var defaultThreadCount: Int = 8,
    var dynamicPartCreationMode: Boolean = true,
    var useServerLastModifiedTime: Boolean = false,
    var globalSpeedLimit: Long = 0,//unlimited
    var useSparseFileAllocation: Boolean = true,
    val minPartSize: Long = 2048,//2kB
    var maxDownloadRetryCount: Int = 0,
    // WARNING: this is used in boot so make sure to update it before booting
    // make it val or add a way to reload it properly
    var appendExtensionToIncompleteDownloads: Boolean = false,
)
