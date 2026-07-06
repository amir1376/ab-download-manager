package ir.amirab.downloader.utils.speedlimiter

import okio.Source

class SpeedLimiter {
    private val myThrottler = OkioCustomizedThrottler()

    fun bytesPerSecond(
        bytesPerSecond: Long
    ) {
        myThrottler.bytesPerSecond(
            bytesPerSecond = bytesPerSecond,
            changeImmediately = true,
        )
    }

    fun source(source: Source): Source {
        return myThrottler.source(source)
    }
}
