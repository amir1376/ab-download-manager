package ir.amirab.downloader

data class DownloadSettings(
    //can be changed after boot!
    var defaultThreadCount: Int = 5,
    var dynamicPartCreationMode:Boolean=true,
    var globalSpeedLimit:Long=0,//unlimited
    val minPartSize: Long = 2048,//2kB
)
