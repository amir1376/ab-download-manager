package com.xeton.downloader.part

import com.xeton.downloader.connection.HttpDownloaderClient
import com.xeton.downloader.connection.Connection
import com.xeton.downloader.connection.response.HttpResponseInfo
import com.xeton.downloader.connection.response.expectSuccess
import com.xeton.downloader.destination.DestWriter
import com.xeton.downloader.downloaditem.http.IHttpDownloadCredentials
import com.xeton.downloader.exception.ServerPartIsNotTheSameAsWeExpectException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import okio.*


/**
 * @param strictMode
 *  `false` - this is not the purpose of its app, so we don't strict here
 *
 *  download part without checking for length validation
 *  this is where we want to only copy data arrived from server
 *  for example, a web page link that maybe get us different response length
 *  we only need to download it no matter what is inside
 *
 *  `true` - main purpose of this class
 *
 *  validate download size before trying to write to the filesystem
 */
class HttpPartDownloader(
    val credentials: IHttpDownloadCredentials,
    getDestWriter: () -> DestWriter,
    part: RangedPart,
    val client: HttpDownloaderClient,
    val speedLimiters: List<Throttler>,
    val strictMode: Boolean,
    partSplitLock: Any,
) : PartDownloader<RangedPart>(
    part = part,
    getDestWriter = getDestWriter
) {


    private suspend fun establishConnection(
        from: Long,
        to: Long?,
    ): Connection<HttpResponseInfo> {
        // [DELEGATED TO RUST]
        // In Phase 1, we bypass OkHttp and use the Rust core `Downloader`.
        // The actual connection logic is now handled internally by xeton_core/http/job.rs
        // This is a placeholder for the actual integration point where Kotlin hands off to Rust.
        throw UnsupportedOperationException("Delegated to Rust XetonCore Downloader")
    }

    override fun onFinish() {
        synchronized(partSplitSupport) {
            if (part.isBlind) {
                part.setBlindAsCompleted()
            }
        }
        super.onFinish()
    }

    private val partSplitSupport = PartSplitSupport(part, partSplitLock)

    //this method is invoked only in one thread for every instance
    override fun howMuchCanRead(maxAllowed: Long): Long {
        return partSplitSupport.howMuchCanRead(
            expandToBufferSize = maxAllowed,
            tryToExtendSafeZone = true
        )
    }

    fun canBeSplit(): Boolean {
        return partSplitSupport.canSplit()
    }

    override suspend fun connectAndVerify(): Connection<HttpResponseInfo> {
        //        thisLogger().info("going to copy data to destination")
        //we copy part because maybe part::to property will change during part split,
        //so we make backup of current part to validate http response
        val partCopy = part.copy()
        val conn = establishConnection(partCopy.current, partCopy.to)
//        thisLogger().info("connection established")
        if (stop || !currentCoroutineContext().isActive) {
            conn.close()
            throw CancellationException()
        }
        val contentLength = conn.contentLength.let {
            if (it == -1L) {
                //in case of no end is come from headers
                null
            } else {
                it
            }
        }
        if (contentLength != partCopy.remainingLength) {
            var throwServerPartIsNotTheSameAsWeExpectException: Boolean
            if (strictMode) {
                throwServerPartIsNotTheSameAsWeExpectException = true
                // allow pass through if the request range start and response range start are the same
                conn.responseInfo.contentRange?.range?.let { range ->
                    if (range.first == partCopy.current) {
                        // if I request from 1..10 then I expect that server give me 1-X the X is not important
                        // but the start should be the same as requested otherwise we can't trust the server response
                        // X may be smaller/bigger than our requested range however we download it as much as we want if it wasn't enough we re request again later
                        throwServerPartIsNotTheSameAsWeExpectException = false
                    }
                }
            } else {
                // just download it we don't want to validate anything here
                throwServerPartIsNotTheSameAsWeExpectException = true
            }
            val serverPartIsNotTheSameAsWeExpectException = ServerPartIsNotTheSameAsWeExpectException(
                start = partCopy.current,
                end = partCopy.to,
                expectedLength = partCopy.remainingLength,
                actualLength = contentLength
            )
            if (throwServerPartIsNotTheSameAsWeExpectException) {
                conn.close()
                throw serverPartIsNotTheSameAsWeExpectException
            } else {
                println("WARNING: ${serverPartIsNotTheSameAsWeExpectException.message}")
            }
        }
        return conn
    }


    //should be sync with part split lock
    fun splitPart(): RangedPart? {
        return partSplitSupport.splitPart()
    }

}
