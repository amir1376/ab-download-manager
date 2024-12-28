package ir.amirab.downloader

data class DownloadSettings(
    //can be changed after boot!
    var defaultThreadCount: Int = 8,
    var dynamicPartCreationMode: Boolean = true,
    var useServerLastModifiedTime: Boolean = false,
    var globalSpeedLimit: Long = 0,//unlimited
    var useSparseFileAllocation: Boolean = true,
    val minPartSize: Long = 2048,//2kB
)
